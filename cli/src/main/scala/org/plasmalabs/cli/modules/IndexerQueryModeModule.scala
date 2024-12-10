package org.plasmalabs.cli.modules

import cats.effect.IO
import org.plasmalabs.cli.controllers.IndexerQueryController
import org.plasmalabs.cli.params.CliParamsParser
import org.plasmalabs.cli.params.models.*
import org.plasmalabs.sdk.dataApi.{IndexerQueryAlgebra, RpcChannelResource}
import scopt.OParser

trait IndexerQueryModeModule extends WalletStateAlgebraModule with RpcChannelResource {

  def indexerQuerySubcmd(
    validateParams: CliParams
  ): IO[Either[String, String]] = validateParams.subcmd match {
    case CliSubCmd.invalid =>
      IO.pure(
        Left(
          OParser.usage(
            CliParamsParser.indexerQueryMode
          ) + "\nA subcommand needs to be specified"
        )
      )
    case CliSubCmd.utxobyaddress =>
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
