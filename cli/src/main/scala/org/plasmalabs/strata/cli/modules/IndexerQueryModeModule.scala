package org.plasmalabs.plasma.cli.modules

import cats.effect.IO
import org.plasmalabs.plasma.cli.controllers.IndexerQueryController
import org.plasmalabs.sdk.dataApi.{IndexerQueryAlgebra, RpcChannelResource}
import org.plasmalabs.plasma.cli.StrataCliSubCmd
import org.plasmalabs.plasma.cli.StrataCliParams
import scopt.OParser
import org.plasmalabs.plasma.cli.StrataCliParamsParserModule

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
