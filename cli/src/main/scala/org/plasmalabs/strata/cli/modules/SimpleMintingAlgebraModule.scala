package org.plasmalabs.strata.cli.modules

import cats.effect.IO
import org.plasmalabs.strata.cli.impl.SimpleMintingAlgebra
import cats.effect.kernel.Sync

trait SimpleMintingAlgebraModule
    extends WalletStateAlgebraModule
    with WalletManagementUtilsModule
    with TransactionBuilderApiModule
    with IndexerQueryAlgebraModule {

  def simpleMintingAlgebra(
      walletFile: String,
      networkId: Int,
      ledgerId: Int,
      host: String,
      nodePort: Int,
      secureConnection: Boolean
  ) = SimpleMintingAlgebra.make[IO](
    Sync[IO],
    walletApi,
    walletStateAlgebra(walletFile),
    walletManagementUtils,
    transactionBuilderApi(networkId, ledgerId),
    indexerQueryAlgebra(host, nodePort, secureConnection)
  )

}
