package co.topl.brambl.cli.modules

import cats.effect.IO
import co.topl.brambl.cli.BramblCliSubCmd
import co.topl.brambl.cli.controllers.WalletController
import co.topl.brambl.dataApi.GenusQueryAlgebra
import co.topl.brambl.cli.BramblCliParams

trait WalletModeModule
    extends WalletStateAlgebraModule
    with WalletManagementUtilsModule
    with WalletApiModule
    with WalletAlgebraModule
    with TransactionBuilderApiModule
    with ChannelResourceModule {

  def walletModeSubcmds(
      validateParams: BramblCliParams
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
      GenusQueryAlgebra
        .make[IO](
          channelResource(
            validateParams.host,
            validateParams.bifrostPort
          )
        )
    )
    validateParams.subcmd match {
      case BramblCliSubCmd.invalid =>
        IO.pure(Left("A subcommand needs to be specified"))
      case BramblCliSubCmd.exportvk =>
        validateParams.someFromState
          .map(x =>
            walletController.exportFinalVk(
              validateParams.someKeyFile.get,
              validateParams.password,
              validateParams.someOutputFile.get,
              validateParams.partyName,
              validateParams.contractName,
              x
            )
          )
          .getOrElse(
            walletController.exportVk(
              validateParams.someKeyFile.get,
              validateParams.password,
              validateParams.someOutputFile.get,
              validateParams.partyName,
              validateParams.contractName
            )
          )
      case BramblCliSubCmd.importvks =>
        walletController.importVk(
          validateParams.network.networkId,
          validateParams.inputVks,
          validateParams.someKeyFile.get,
          validateParams.password,
          validateParams.contractName,
          validateParams.partyName
        )
      case BramblCliSubCmd.init =>
        walletController.createWalletFromParams(validateParams)
      case BramblCliSubCmd.recoverkeys =>
        walletController.recoverKeysFromParams(validateParams)
      case BramblCliSubCmd.sync =>
        walletController.sync(
          validateParams.network.networkId,
          validateParams.contractName,
          validateParams.partyName
        )
      case BramblCliSubCmd.currentaddress =>
        walletController.currentaddress()
    }
  }
}
