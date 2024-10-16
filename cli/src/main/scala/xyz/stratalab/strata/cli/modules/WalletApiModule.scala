package xyz.stratalab.strata.cli.modules

import xyz.stratalab.sdk.wallet.WalletApi

trait WalletApiModule extends WalletKeyApiModule {
  val walletApi = WalletApi.make(walletKeyApi)
}
