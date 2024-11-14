package org.plasmalabs.cli.modules

import cats.effect.IO
import org.plasmalabs.cli.controllers.IndexerQueryController
import org.plasmalabs.sdk.dataApi.{IndexerQueryAlgebra, RpcChannelResource}
import org.plasmalabs.cli.PlasmaCliSubCmd
import org.plasmalabs.cli.PlasmaCliParams
import scopt.OParser
import org.plasmalabs.cli.PlasmaCliParamsParserModule

trait IndexerQueryModeModule extends WalletStateAlgebraModule with RpcChannelResource {

  def indexerQuerySubcmd(
    validateParams: PlasmaCliParams
  ): IO[Either[String, String]] = validateParams.subcmd match {
    case PlasmaCliSubCmd.invalid =>
      IO.pure(
        Left(
          OParser.usage(
            PlasmaCliParamsParserModule.indexerQueryMode
          ) + "\nA subcommand needs to be specified"
        )
      )
    case PlasmaCliSubCmd.utxobyaddress =>
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
