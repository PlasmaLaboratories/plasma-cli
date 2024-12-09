package org.plasmalabs.cli.modules

import cats.effect.IO
import org.plasmalabs.sdk.dataApi.WalletKeyApiAlgebra
import org.plasmalabs.sdk.servicekit.WalletKeyApi
import org.plasmalabs.sdk.wallet.WalletApi

trait WalletApiModule {

  val walletKeyApi: WalletKeyApiAlgebra[IO] = WalletKeyApi.make[IO]()
  val walletApi: WalletApi[IO] = WalletApi.make(walletKeyApi)
}
