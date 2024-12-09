package org.plasmalabs.cli.modules

import cats.effect.IO
import org.plasmalabs.cli.impl.SimpleTransactionAlgebra
import org.plasmalabs.sdk.constants.NetworkConstants

trait SimpleTransactionAlgebraModule
    extends WalletStateAlgebraModule
    with WalletManagementUtilsModule
    with IndexerQueryAlgebraModule {

  def simplTransactionOps(
    walletFile:       String,
    networkId:        Int,
    host:             String,
    nodePort:         Int,
    secureConnection: Boolean
  ): SimpleTransactionAlgebra[IO] = SimpleTransactionAlgebra
    .make[IO](
      walletApi,
      walletStateAlgebra(walletFile),
      indexerQueryAlgebra(
        host,
        nodePort,
        secureConnection
      ),
      transactionBuilderApi(
        networkId,
        NetworkConstants.MAIN_LEDGER_ID
      ),
      walletManagementUtils
    )
}
