package xyz.stratalab.strata.cli.modules

import cats.effect.IO
import xyz.stratalab.strata.cli.controllers.NodeQueryController
import xyz.stratalab.strata.cli.StrataCliSubCmd
import xyz.stratalab.sdk.dataApi.{NodeQueryAlgebra, RpcChannelResource}
import xyz.stratalab.strata.cli.StrataCliParams
import scopt.OParser
import xyz.stratalab.strata.cli.StrataCliParamsParserModule

trait NodeQueryModeModule extends RpcChannelResource {

  def nodeQuerySubcmd(
      validateParams: StrataCliParams
  ): IO[Either[String, String]] = {
    val nodeQueryAlgebra = NodeQueryAlgebra.make[IO](
      channelResource(
        validateParams.host,
        validateParams.nodePort,
        validateParams.secureConnection
      )
    )
    validateParams.subcmd match {
      case StrataCliSubCmd.invalid =>
        IO.pure(
          Left(
            OParser.usage(
              StrataCliParamsParserModule.nodeQueryMode
            ) + "\nA subcommand needs to be specified"
          )
        )
      case StrataCliSubCmd.mintblock =>
        new NodeQueryController(nodeQueryAlgebra)
          .makeBlock(
            validateParams.nbOfBlocks
          )
      case StrataCliSubCmd.blockbyheight =>
        new NodeQueryController(
          nodeQueryAlgebra
        ).blockByHeight(validateParams.height)
      case StrataCliSubCmd.blockbyid =>
        new NodeQueryController(
          nodeQueryAlgebra
        ).blockById(validateParams.blockId)
      case StrataCliSubCmd.transactionbyid =>
        new NodeQueryController(
          nodeQueryAlgebra
        ).fetchTransaction(validateParams.transactionId)
    }
  }

}
