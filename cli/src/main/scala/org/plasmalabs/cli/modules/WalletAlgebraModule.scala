package org.plasmalabs.cli.modules

import cats.effect.IO
import org.plasmalabs.cli.impl.WalletAlgebra

trait WalletAlgebraModule extends WalletStateAlgebraModule with WalletApiModule {

  def walletAlgebra(file: String): WalletAlgebra[IO] = WalletAlgebra.make(
    walletApi,
    walletStateAlgebra(file)
  )
}
