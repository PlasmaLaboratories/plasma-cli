package org.plasmalabs.cli.modules

import cats.data.Kleisli
import cats.effect.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.io.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.http4s.server.staticcontent.resourceServiceBuilder
import org.plasmalabs.cli.http.WalletHttpService
import org.plasmalabs.cli.params.CliParamsParser
import org.plasmalabs.cli.params.models.*
import org.plasmalabs.sdk.codecs.AddressCodecs
import org.plasmalabs.shared.models.{TxRequest, TxResponse}
import scopt.OParser

import java.nio.file.Files

trait ServerModule extends FellowshipsModeModule with WalletModeModule with FullTxModule {

  lazy val httpService: HttpRoutes[IO] = HttpRoutes.of[IO] {

    // You must serve the index.html file that loads your frontend code for
    // every url that is defined in your frontend (Waypoint) routes, in order
    // for users to be able to navigate to these URLs from outside of your app.
    case request @ GET -> Root =>
      StaticFile
        .fromResource("/static/index.html", Some(request))
        .getOrElseF(InternalServerError())

    // This route covers all URLs under `/app`, including `/app` and `/app/`.
    case request @ GET -> "app" /: _ =>
      StaticFile
        .fromResource("/static/index.html", Some(request))
        .getOrElseF(InternalServerError())

    // Vite moves index.html into the public directory, but we don't want
    // users to navigate manually to /index.html in the browser, because
    // that route is not defined in Waypoint, we use `/` instead.
    case GET -> Root / "index.html" =>
      TemporaryRedirect(headers.Location(Uri.fromString("/").toOption.get))
  }

  def apiServices(validateParams: CliParams): HttpRoutes[IO] = HttpRoutes.of[IO] { case req @ POST -> Root / "send" =>
    implicit val txReqDecoder: EntityDecoder[IO, TxRequest] =
      jsonOf[IO, TxRequest]

    for {
      input <- req.as[TxRequest]
      result <- sendFunds(
        validateParams.network,
        validateParams.password,
        validateParams.walletFile,
        validateParams.someKeyFile.get,
        input.fromFellowship,
        input.fromTemplate,
        input.fromInteraction.map(_.toInt),
        Some(input.fromFellowship),
        Some(input.fromTemplate),
        input.fromInteraction.map(_.toInt),
        AddressCodecs.decodeAddress(input.address).toOption,
        input.amount.toLong,
        input.fee.toLong,
        input.token,
        Files.createTempFile("txFile", ".pbuf").toAbsolutePath().toString(),
        Files
          .createTempFile("provedTxFile", ".pbuf")
          .toAbsolutePath()
          .toString(),
        validateParams.host,
        validateParams.nodePort,
        validateParams.secureConnection
      )
      resp <- Ok(TxResponse(result).asJson)
    } yield resp
  }

  def serverSubcmd(
    validateParams: CliParams
  ): IO[Either[String, String]] = validateParams.subcmd match {
    case CliSubCmd.invalid =>
      IO.pure(
        Left(
          OParser.usage(
            CliParamsParser.serverMode
          ) + "\nA subcommand needs to be specified"
        )
      )
    case CliSubCmd.init =>
      val staticAssetsService = resourceServiceBuilder[IO]("/static").toRoutes
      val logger =
        org.typelevel.log4cats.slf4j.Slf4jLogger.getLoggerFromName[IO]("App")
      (for {
        notFoundResponse <- Resource.make(
          NotFound(
            """<!DOCTYPE html>
          |<html>
          |<body>
          |<h1>Not found</h1>
          |<p>The page you are looking for is not found.</p>
          |<p>This message was generated on the server.</p>
          |</body>
          |</html>""".stripMargin('|'),
            headers.`Content-Type`(MediaType.text.html)
          )
        )(_ => IO.unit)

        app = {
          val router = Router.define(
            "/" -> httpService,
            "/api/wallet" -> WalletHttpService(
              walletStateAlgebra(
                validateParams.walletFile
              ),
              channelResource(
                validateParams.host,
                validateParams.nodePort,
                validateParams.secureConnection
              ),
              walletResource(validateParams.walletFile)
            ).walletService(
              validateParams.network.name,
              validateParams.network.networkId.toString()
            ),
            "/api/tx" -> apiServices(validateParams)
          )(default = staticAssetsService)

          Kleisli[IO, Request[IO], Response[IO]] { request =>
            router.run(request).getOrElse(notFoundResponse)
          }
        }

        _ <- EmberServerBuilder
          .default[IO]
          .withIdleTimeout(ServerConfig.idleTimeOut)
          .withHost(ServerConfig.host)
          .withPort(ServerConfig.port)
          .withHttpApp(app)
          .withLogger(logger)
          .build

      } yield Right(
        s"Server started on ${ServerConfig.host}:${ServerConfig.port}"
      )).allocated
        .map(_._1)
        .handleErrorWith { e =>
          IO {
            Left(e.getMessage)
          }
        } >> IO.never
  }
}
