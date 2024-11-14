package org.plasmalabs.cli.modules

import cats.effect.IO
import org.plasmalabs.cli.impl.TransactionAlgebra
import org.plasmalabs.sdk.dataApi.RpcChannelResource

trait TransactionAlgebraModule
    extends WalletStateAlgebraModule
    with WalletManagementUtilsModule
    with RpcChannelResource {

  def transactionOps(
    walletFile:       String,
    host:             String,
    port:             Int,
    secureConnection: Boolean
  ) = TransactionAlgebra
    .make[IO](
      walletApi,
      walletStateAlgebra(walletFile),
      walletManagementUtils,
      channelResource(
        host,
        port,
        secureConnection
      )
    )
}
