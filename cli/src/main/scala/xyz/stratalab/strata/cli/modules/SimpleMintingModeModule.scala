package org.plasmalabs.strata.cli.modules

import cats.effect.IO
import org.plasmalabs.strata.cli.StrataCliParams
import org.plasmalabs.strata.cli.StrataCliSubCmd
import org.plasmalabs.strata.cli.TokenType
import org.plasmalabs.strata.cli.controllers.SimpleMintingController
import org.plasmalabs.strata.cli.impl.GroupPolicyParserModule
import org.plasmalabs.strata.cli.impl.SeriesPolicyParserModule
import org.plasmalabs.sdk.constants.NetworkConstants
import org.plasmalabs.strata.cli.impl.AssetStatementParserModule
import scopt.OParser
import org.plasmalabs.strata.cli.StrataCliParamsParserModule

trait SimpleMintingModeModule
    extends GroupPolicyParserModule
    with SeriesPolicyParserModule
    with AssetStatementParserModule
    with SimpleMintingAlgebraModule {

  def simpleMintingSubcmds(
      validateParams: StrataCliParams
  ): IO[Either[String, String]] = {
    val simpleMintingController = new SimpleMintingController(
      groupPolicyParserAlgebra(validateParams.network.networkId),
      seriesPolicyParserAlgebra(validateParams.network.networkId),
      assetMintingStatementParserAlgebra(validateParams.network.networkId),
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
      case StrataCliSubCmd.invalid =>
        IO.pure(
          Left(
            OParser.usage(
              StrataCliParamsParserModule.simpleMintingMode
            ) + "\nA subcommand needs to be specified"
          )
        )
      case StrataCliSubCmd.create =>
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
