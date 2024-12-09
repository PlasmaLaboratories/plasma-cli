package org.plasmalabs.cli.modules

import cats.effect.IO
import org.plasmalabs.cli.controllers.SimpleMintingController
import org.plasmalabs.cli.params.CliParamsParser
import org.plasmalabs.cli.params.models.*
import org.plasmalabs.sdk.constants.NetworkConstants
import scopt.OParser

trait SimpleMintingModeModule
    extends ParserModule.Group
    with ParserModule.Series
    with ParserModule.Ams
    with SimpleMintingAlgebraModule {

  def simpleMintingSubcmds(
    validateParams: CliParams
  ): IO[Either[String, String]] = {
    val simpleMintingController = new SimpleMintingController(
      groupPolicyParser(validateParams.network.networkId),
      seriesPolicyParser(validateParams.network.networkId),
      assetMintingStatementParser(validateParams.network.networkId),
      simpleMintingAlgebra(
        validateParams.walletFile,
        validateParams.network.networkId,
        NetworkConstants.MAIN_LEDGER_ID,
        validateParams.host,
        validateParams.nodePort,
        validateParams.secureConnection
      )
    )
    validateParams.subcmd match {
      case CliSubCmd.invalid =>
        IO.pure(
          Left(
            OParser.usage(
              CliParamsParser.simpleMintingMode
            ) + "\nA subcommand needs to be specified"
          )
        )
      case CliSubCmd.create =>
        validateParams.tokenType match {
          case TokenType.group =>
            simpleMintingController
              .createSimpleGroupMintingTransactionFromParams(
                validateParams.someInputFile.get,
                validateParams.someKeyFile.get,
                validateParams.password,
                validateParams.fromFellowship,
                validateParams.fromTemplate,
                validateParams.someFromInteraction,
                validateParams.amount,
                validateParams.fee,
                validateParams.someOutputFile.get
              )
          case TokenType.series =>
            simpleMintingController
              .createSimpleSeriesMintingTransactionFromParams(
                validateParams.someInputFile.get,
                validateParams.someKeyFile.get,
                validateParams.password,
                validateParams.fromFellowship,
                validateParams.fromTemplate,
                validateParams.someFromInteraction,
                validateParams.amount,
                validateParams.fee,
                validateParams.someOutputFile.get
              )
          case TokenType.asset =>
            simpleMintingController
              .createSimpleAssetMintingTransactionFromParams(
                validateParams.someInputFile.get,
                validateParams.someKeyFile.get,
                validateParams.password,
                validateParams.fromFellowship,
                validateParams.fromTemplate,
                validateParams.someFromInteraction,
                validateParams.fee,
                validateParams.ephemeralMetadata,
                validateParams.someCommitment,
                validateParams.someOutputFile.get
              )
        }
    }
  }
}
