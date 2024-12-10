package org.plasmalabs.cli.modules

import cats.effect.IO
import org.plasmalabs.cli.impl.SimpleMintingAlgebra

trait SimpleMintingAlgebraModule
    extends WalletStateAlgebraModule
    with WalletManagementUtilsModule
    with TransactionBuilderApiModule
    with IndexerQueryAlgebraModule {

  def simpleMintingAlgebra(
    walletFile:       String,
    networkId:        Int,
    ledgerId:         Int,
    host:             String,
    nodePort:         Int,
    secureConnection: Boolean
  ): SimpleMintingAlgebra[IO] = SimpleMintingAlgebra.make[IO](
    walletApi,
    walletStateAlgebra(walletFile),
    walletManagementUtils,
    transactionBuilderApi(networkId, ledgerId),
    indexerQueryAlgebra(host, nodePort, secureConnection)
  )

}
