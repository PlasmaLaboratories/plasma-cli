package org.plasmalabs.cli.modules

import cats.effect.IO
import org.plasmalabs.cli.PlasmaCliParams
import org.plasmalabs.cli.PlasmaCliSubCmd
import org.plasmalabs.cli.TokenType
import org.plasmalabs.cli.controllers.SimpleMintingController
import org.plasmalabs.cli.impl.GroupPolicyParserModule
import org.plasmalabs.cli.impl.SeriesPolicyParserModule
import org.plasmalabs.sdk.constants.NetworkConstants
import org.plasmalabs.cli.impl.AssetStatementParserModule
import scopt.OParser
import org.plasmalabs.cli.PlasmaCliParamsParserModule

trait SimpleMintingModeModule
    extends GroupPolicyParserModule
    with SeriesPolicyParserModule
    with AssetStatementParserModule
    with SimpleMintingAlgebraModule {

  def simpleMintingSubcmds(
      validateParams: PlasmaCliParams
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
      case PlasmaCliSubCmd.invalid =>
        IO.pure(
          Left(
            OParser.usage(
              PlasmaCliParamsParserModule.simpleMintingMode
            ) + "\nA subcommand needs to be specified"
          )
        )
      case PlasmaCliSubCmd.create =>
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
