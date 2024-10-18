package org.plasmalabs.strata.cli.modules

import cats.effect.IO
import org.plasmalabs.strata.cli.StrataCliParams
import org.plasmalabs.strata.cli.StrataCliSubCmd
import org.plasmalabs.strata.cli.controllers.TxController
import org.plasmalabs.sdk.constants.NetworkConstants
import scopt.OParser
import org.plasmalabs.strata.cli.StrataCliParamsParserModule

trait TxModeModule extends TxParserAlgebraModule with TransactionAlgebraModule {

  def txModeSubcmds(
      validateParams: StrataCliParams
  ): IO[Either[String, String]] = {
    validateParams.subcmd match {
      case StrataCliSubCmd.invalid =>
        IO.pure(
          Left(
            OParser.usage(
              StrataCliParamsParserModule.transactionMode
            ) + "\nA subcommand needs to be specified"
          )
        )
      case StrataCliSubCmd.broadcast =>
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
      case StrataCliSubCmd.prove =>
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
      case StrataCliSubCmd.inspect =>
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
      case StrataCliSubCmd.create =>
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

}
