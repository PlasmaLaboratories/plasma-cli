package org.plasmalabs.cli.modules

import org.plasmalabs.cli.impl.WalletManagementUtils
import cats.effect.IO

trait WalletManagementUtilsModule extends WalletApiModule {

  val walletManagementUtils =
    new WalletManagementUtils[IO](walletApi, walletKeyApi)
}
