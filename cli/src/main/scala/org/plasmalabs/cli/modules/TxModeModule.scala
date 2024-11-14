package org.plasmalabs.cli.modules

import cats.effect.IO
import org.plasmalabs.cli.PlasmaCliParams
import org.plasmalabs.cli.PlasmaCliSubCmd
import org.plasmalabs.cli.controllers.TxController
import org.plasmalabs.sdk.constants.NetworkConstants
import scopt.OParser
import org.plasmalabs.cli.PlasmaCliParamsParserModule

trait TxModeModule extends TxParserAlgebraModule with TransactionAlgebraModule {

  def txModeSubcmds(
    validateParams: PlasmaCliParams
  ): IO[Either[String, String]] =
    validateParams.subcmd match {
      case PlasmaCliSubCmd.invalid =>
        IO.pure(
          Left(
            OParser.usage(
              PlasmaCliParamsParserModule.transactionMode
            ) + "\nA subcommand needs to be specified"
          )
        )
      case PlasmaCliSubCmd.broadcast =>
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
      case PlasmaCliSubCmd.prove =>
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
      case PlasmaCliSubCmd.inspect =>
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
        ).inspectTransaction(validateParams.someInputFile.get)
      case PlasmaCliSubCmd.create =>
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
