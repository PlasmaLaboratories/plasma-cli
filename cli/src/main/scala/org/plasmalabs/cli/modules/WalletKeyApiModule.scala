package org.plasmalabs.cli.modules

import cats.effect.IO
import org.plasmalabs.sdk.servicekit.WalletKeyApi

trait WalletKeyApiModule {
  val walletKeyApi = WalletKeyApi.make[IO]()
}
