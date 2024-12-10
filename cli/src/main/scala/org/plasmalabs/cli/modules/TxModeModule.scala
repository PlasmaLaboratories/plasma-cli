package org.plasmalabs.cli.modules

import cats.effect.IO
import org.plasmalabs.cli.controllers.{SimpleController, TxController}
import org.plasmalabs.cli.params.CliParamsParser
import org.plasmalabs.cli.params.models.*
import org.plasmalabs.sdk.constants.NetworkConstants
import scopt.OParser

trait TxModeModule extends TxParserAlgebraModule with TransactionAlgebraModule {

  def txModeSubcmds(
    validateParams: CliParams
  ): IO[Either[String, String]] =
    validateParams.subcmd match {
      case CliSubCmd.invalid =>
        IO.pure(
          Left(
            OParser.usage(
              CliParamsParser.transactionMode
            ) + "\nA subcommand needs to be specified"
          )
        )
      case CliSubCmd.broadcast =>
        new TxController(
          txParserAlgebra(
            validateParams.network.networkId,
            NetworkConstants.MAIN_LEDGER_ID
          ),
          transactionOps(
            validateParams.walletFile,
            validateParams.host,
            validateParams.nodePort,
            validateParams.secureConnection
          )
        ).broadcastSimpleTransactionFromParams(validateParams.someInputFile.get)
      case CliSubCmd.prove =>
        new TxController(
          txParserAlgebra(
            validateParams.network.networkId,
            NetworkConstants.MAIN_LEDGER_ID
          ),
          transactionOps(
            validateParams.walletFile,
            validateParams.host,
            validateParams.nodePort,
            validateParams.secureConnection
          )
        ).proveSimpleTransactionFromParams(
          validateParams.someInputFile.get,
          validateParams.someKeyFile.get,
          validateParams.password,
          validateParams.someOutputFile.get
        )
      case CliSubCmd.inspect =>
        SimpleController
          .inspectTransaction(validateParams.someInputFile.get)
      case CliSubCmd.create =>
        new TxController(
          txParserAlgebra(
            validateParams.network.networkId,
            NetworkConstants.MAIN_LEDGER_ID
          ),
          transactionOps(
            validateParams.walletFile,
            validateParams.host,
            validateParams.nodePort,
            validateParams.secureConnection
          )
        )
          .createComplexTransaction(
            validateParams.someInputFile.get,
            validateParams.someOutputFile.get
          )
    }

}
