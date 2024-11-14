package org.plasmalabs.cli.modules

import cats.effect.IO
import org.plasmalabs.cli.PlasmaCliParams
import org.plasmalabs.cli.PlasmaCliSubCmd
import org.plasmalabs.cli.controllers.WalletController
import org.plasmalabs.sdk.dataApi.{IndexerQueryAlgebra, RpcChannelResource}
import scopt.OParser
import org.plasmalabs.cli.PlasmaCliParamsParserModule

trait WalletModeModule
    extends WalletStateAlgebraModule
    with WalletManagementUtilsModule
    with WalletApiModule
    with WalletAlgebraModule
    with TransactionBuilderApiModule
    with RpcChannelResource {

  def walletModeSubcmds(
      validateParams: PlasmaCliParams
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
      case PlasmaCliSubCmd.balance =>
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
      case PlasmaCliSubCmd.addsecret =>
        walletController.addSecret(validateParams.secret, validateParams.digest)
      case PlasmaCliSubCmd.getpreimage =>
        walletController.getPreimage(
          validateParams.digest,
          validateParams.digestText
        )
      case PlasmaCliSubCmd.invalid =>
        IO.pure(
          Left(
            OParser.usage(
              PlasmaCliParamsParserModule.walletMode
            ) + "\nA subcommand needs to be specified"
          )
        )
      case PlasmaCliSubCmd.exportvk =>
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
      case PlasmaCliSubCmd.importvks =>
        walletController.importVk(
          validateParams.network.networkId,
          validateParams.inputVks,
          validateParams.someKeyFile.get,
          validateParams.password,
          validateParams.templateName,
          validateParams.fellowshipName
        )
      case PlasmaCliSubCmd.listinteraction =>
        walletController.listInteractions(
          validateParams.fellowshipName,
          validateParams.templateName
        )
      case PlasmaCliSubCmd.init =>
        walletController.createWalletFromParams(validateParams)
      case PlasmaCliSubCmd.recoverkeys =>
        walletController.recoverKeysFromParams(validateParams)
      case PlasmaCliSubCmd.setinteraction =>
        walletController.setCurrentInteraction(
          validateParams.fromFellowship,
          validateParams.fromTemplate,
          validateParams.someFromInteraction.get
        )
      case PlasmaCliSubCmd.sync =>
        walletController.sync(
          validateParams.network.networkId,
          validateParams.fellowshipName,
          validateParams.templateName
        )
      case PlasmaCliSubCmd.currentaddress =>
        walletController.currentaddress(
          validateParams
        )
    }
  }
}
