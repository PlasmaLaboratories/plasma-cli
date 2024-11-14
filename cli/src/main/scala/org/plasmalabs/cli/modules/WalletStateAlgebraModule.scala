package org.plasmalabs.cli.modules

import cats.effect.IO
import org.plasmalabs.sdk.servicekit.{WalletStateApi, WalletStateResource}

trait WalletStateAlgebraModule extends WalletStateResource with WalletApiModule with TransactionBuilderApiModule {

  def walletStateAlgebra(file: String) = WalletStateApi
    .make[IO](
      walletResource(file),
      walletApi
    )
}
