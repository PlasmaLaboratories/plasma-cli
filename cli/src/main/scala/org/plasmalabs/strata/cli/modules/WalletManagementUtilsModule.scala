package org.plasmalabs.plasma.cli.modules

import org.plasmalabs.plasma.cli.impl.WalletManagementUtils
import cats.effect.IO

trait WalletManagementUtilsModule extends WalletApiModule {

  val walletManagementUtils =
    new WalletManagementUtils[IO](walletApi, walletKeyApi)
}
