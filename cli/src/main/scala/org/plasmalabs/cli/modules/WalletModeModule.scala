package org.plasmalabs.cli.modules

import cats.effect.IO
import org.plasmalabs.cli.controllers.WalletController
import org.plasmalabs.cli.params.CliParamsParser
import org.plasmalabs.cli.params.models.*
import org.plasmalabs.sdk.dataApi.{IndexerQueryAlgebra, RpcChannelResource}
import scopt.OParser

trait WalletModeModule
    extends WalletStateAlgebraModule
    with WalletManagementUtilsModule
    with WalletApiModule
    with WalletAlgebraModule
    with TransactionBuilderApiModule
    with RpcChannelResource {

  def walletModeSubcmds(
    validateParams: CliParams
  ): IO[Either[String, String]] = {
    val walletController = new WalletController(
      walletStateAlgebra(
        validateParams.walletFile
      ),
      walletManagementUtils,
      walletApi,
      walletAlgebra(
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
    )
    validateParams.subcmd match {
      case CliSubCmd.balance =>
        walletController.getBalance(
          validateParams.fromAddress,
          if (validateParams.fromAddress.isEmpty)
            Some(validateParams.fromFellowship)
          else None,
          if (validateParams.fromAddress.isEmpty)
            Some(validateParams.fromTemplate)
          else None,
          validateParams.someFromInteraction
        )
      case CliSubCmd.addsecret =>
        walletController.addSecret(validateParams.secret, validateParams.digest)
      case CliSubCmd.getpreimage =>
        walletController.getPreimage(
          validateParams.digest,
          validateParams.digestText
        )
      case CliSubCmd.invalid =>
        IO.pure(
          Left(
            OParser.usage(
              CliParamsParser.walletMode
            ) + "\nA subcommand needs to be specified"
          )
        )
      case CliSubCmd.exportvk =>
        validateParams.someFromInteraction
          .map(x =>
            walletController.exportFinalVk(
              validateParams.someKeyFile.get,
              validateParams.password,
              validateParams.someOutputFile.get,
              validateParams.fellowshipName,
              validateParams.templateName,
              x
            )
          )
          .getOrElse(
            walletController.exportVk(
              validateParams.someKeyFile.get,
              validateParams.password,
              validateParams.someOutputFile.get,
              validateParams.fellowshipName,
              validateParams.templateName
            )
          )
      case CliSubCmd.importvks =>
        walletController.importVk(
          validateParams.network.networkId,
          validateParams.inputVks,
          validateParams.someKeyFile.get,
          validateParams.password,
          validateParams.templateName,
          validateParams.fellowshipName
        )
      case CliSubCmd.listinteraction =>
        walletController.listInteractions(
          validateParams.fellowshipName,
          validateParams.templateName
        )
      case CliSubCmd.init =>
        walletController.createWalletFromParams(validateParams)
      case CliSubCmd.recoverkeys =>
        walletController.recoverKeysFromParams(validateParams)
      case CliSubCmd.setinteraction =>
        walletController.setCurrentInteraction(
          validateParams.fromFellowship,
          validateParams.fromTemplate,
          validateParams.someFromInteraction.get
        )
      case CliSubCmd.sync =>
        walletController.sync(
          validateParams.network.networkId,
          validateParams.fellowshipName,
          validateParams.templateName
        )
      case CliSubCmd.currentaddress =>
        walletController.currentaddress(
          validateParams
        )
    }
  }
}
