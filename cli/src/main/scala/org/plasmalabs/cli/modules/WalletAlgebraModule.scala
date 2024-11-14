package org.plasmalabs.cli.modules

import org.plasmalabs.cli.impl.WalletAlgebra

trait WalletAlgebraModule extends WalletStateAlgebraModule with WalletApiModule {

  def walletAlgebra(file: String) = WalletAlgebra.make(
    walletApi,
    walletStateAlgebra(file)
  )
}
