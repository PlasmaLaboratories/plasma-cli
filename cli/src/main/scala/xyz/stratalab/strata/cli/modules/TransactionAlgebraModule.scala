package xyz.stratalab.strata.cli.modules

import cats.effect.IO
import xyz.stratalab.strata.cli.impl.TransactionAlgebra
import xyz.stratalab.sdk.dataApi.RpcChannelResource

trait TransactionAlgebraModule
    extends WalletStateAlgebraModule
    with WalletManagementUtilsModule
    with RpcChannelResource {

  def transactionOps(
      walletFile: String,
      host: String,
      port: Int,
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
