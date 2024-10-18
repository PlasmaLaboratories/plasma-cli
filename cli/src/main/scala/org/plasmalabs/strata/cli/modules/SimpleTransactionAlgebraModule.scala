package org.plasmalabs.strata.cli.modules

import org.plasmalabs.sdk.constants.NetworkConstants
import cats.effect.IO
import org.plasmalabs.strata.cli.impl.SimpleTransactionAlgebra

trait SimpleTransactionAlgebraModule
    extends WalletStateAlgebraModule
    with WalletManagementUtilsModule
    with IndexerQueryAlgebraModule {

  def simplTransactionOps(
      walletFile: String,
      networkId: Int,
      host: String,
      nodePort: Int,
      secureConnection: Boolean
  ) = SimpleTransactionAlgebra
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
