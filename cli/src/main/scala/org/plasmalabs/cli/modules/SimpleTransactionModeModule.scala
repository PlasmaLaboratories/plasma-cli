package org.plasmalabs.cli.modules

import cats.effect.IO
import org.plasmalabs.cli.controllers.SimpleTransactionController
import org.plasmalabs.cli.{PlasmaCliParams, PlasmaCliParamsParserModule, PlasmaCliSubCmd}
import scopt.OParser

trait SimpleTransactionModeModule extends SimpleTransactionAlgebraModule with WalletStateAlgebraModule {

  def simpleTransactionSubcmds(
    validateParams: PlasmaCliParams
  ): IO[Either[String, String]] = validateParams.subcmd match {
    case PlasmaCliSubCmd.invalid =>
      IO.pure(
        Left(
          OParser.usage(
            PlasmaCliParamsParserModule.simpleTransactionMode
          ) + "\nA subcommand needs to be specified"
        )
      )
    case PlasmaCliSubCmd.create =>
      new SimpleTransactionController(
        walletStateAlgebra(
          validateParams.walletFile
        ),
        simplTransactionOps(
          validateParams.walletFile,
          validateParams.network.networkId,
          validateParams.host,
          validateParams.nodePort,
          validateParams.secureConnection
        )
      ).createSimpleTransactionFromParams(
        validateParams.someKeyFile.get,
        validateParams.password,
        (
          validateParams.fromFellowship,
          validateParams.fromTemplate,
          validateParams.someFromInteraction
        ),
        (
          validateParams.someChangeFellowship,
          validateParams.someChangeTemplate,
          validateParams.someChangeInteraction
        ),
        validateParams.toAddress,
        validateParams.someToFellowship,
        validateParams.someToTemplate,
        validateParams.amount,
        validateParams.fee,
        validateParams.someOutputFile.get,
        validateParams.tokenType,
        validateParams.someGroupId,
        validateParams.someSeriesId
      )
  }
}
