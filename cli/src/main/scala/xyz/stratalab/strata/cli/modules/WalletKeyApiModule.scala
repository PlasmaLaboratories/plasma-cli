package xyz.stratalab.strata.cli.modules

import cats.effect.IO
import xyz.stratalab.sdk.servicekit.WalletKeyApi

trait WalletKeyApiModule {
  val walletKeyApi = WalletKeyApi.make[IO]()
}
