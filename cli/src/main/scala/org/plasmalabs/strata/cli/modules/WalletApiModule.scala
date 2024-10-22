package org.plasmalabs.plasma.cli.modules

import org.plasmalabs.sdk.wallet.WalletApi

trait WalletApiModule extends WalletKeyApiModule {
  val walletApi = WalletApi.make(walletKeyApi)
}
