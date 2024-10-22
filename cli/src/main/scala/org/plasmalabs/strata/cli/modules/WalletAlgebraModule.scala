package org.plasmalabs.plasma.cli.modules

import org.plasmalabs.plasma.cli.impl.WalletAlgebra

trait WalletAlgebraModule
    extends WalletStateAlgebraModule
    with WalletApiModule {
  def walletAlgebra(file: String) = WalletAlgebra.make(
    walletApi,
    walletStateAlgebra(file)
  )
}
