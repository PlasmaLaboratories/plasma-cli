package xyz.stratalab.strata.cli.modules

import cats.effect.IO
import xyz.stratalab.strata.cli.controllers.IndexerQueryController
import xyz.stratalab.sdk.dataApi.{IndexerQueryAlgebra, RpcChannelResource}
import xyz.stratalab.strata.cli.StrataCliSubCmd
import xyz.stratalab.strata.cli.StrataCliParams
import scopt.OParser
import xyz.stratalab.strata.cli.StrataCliParamsParserModule

trait IndexerQueryModeModule
    extends WalletStateAlgebraModule
    with RpcChannelResource {

  def indexerQuerySubcmd(
      validateParams: StrataCliParams
  ): IO[Either[String, String]] = validateParams.subcmd match {
    case StrataCliSubCmd.invalid =>
      IO.pure(
        Left(
          OParser.usage(
            StrataCliParamsParserModule.indexerQueryMode
          ) + "\nA subcommand needs to be specified"
        )
      )
    case StrataCliSubCmd.utxobyaddress =>
      new IndexerQueryController(
        walletStateAlgebra(
          validateParams.walletFile
        ),
        IndexerQueryAlgebra
          .make[IO](
            channelResource(
              validateParams.host,
              validateParams.nodePort,
              validateParams.secureConnection
            )
          )
      ).queryUtxoFromParams(
        validateParams.fromAddress,
        validateParams.fromFellowship,
        validateParams.fromTemplate,
        validateParams.someFromInteraction,
        validateParams.tokenType
      )
  }

}
