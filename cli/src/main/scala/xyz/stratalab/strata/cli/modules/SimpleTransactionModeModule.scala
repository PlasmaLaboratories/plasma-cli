package xyz.stratalab.strata.cli.modules

import cats.effect.IO
import xyz.stratalab.strata.cli.StrataCliSubCmd
import xyz.stratalab.strata.cli.controllers.SimpleTransactionController
import xyz.stratalab.strata.cli.StrataCliParams
import scopt.OParser
import xyz.stratalab.strata.cli.StrataCliParamsParserModule

trait SimpleTransactionModeModule
    extends SimpleTransactionAlgebraModule
    with WalletStateAlgebraModule {

  def simpleTransactionSubcmds(
      validateParams: StrataCliParams
  ): IO[Either[String, String]] = validateParams.subcmd match {
    case StrataCliSubCmd.invalid =>
        IO.pure(
          Left(
            OParser.usage(
              StrataCliParamsParserModule.simpleTransactionMode
            ) + "\nA subcommand needs to be specified"
          )
        )
    case StrataCliSubCmd.create =>
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
