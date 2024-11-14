package org.plasmalabs.cli.modules

import cats.effect.IO
import org.plasmalabs.cli.impl.WalletManagementUtils

trait WalletManagementUtilsModule extends WalletApiModule {

  val walletManagementUtils =
    new WalletManagementUtils[IO](walletApi, walletKeyApi)
}
