package xyz.stratalab.strata.cli.modules

import cats.effect.IO
import xyz.stratalab.sdk.servicekit.WalletStateApi
import xyz.stratalab.sdk.servicekit.WalletStateResource

trait WalletStateAlgebraModule
    extends WalletStateResource
    with WalletApiModule
    with TransactionBuilderApiModule {

  def walletStateAlgebra(file: String) = WalletStateApi
    .make[IO](
      walletResource(file),
      walletApi
    )
}
