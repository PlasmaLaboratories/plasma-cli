package xyz.stratalab.strata.cli.modules

import cats.effect.IO
import xyz.stratalab.sdk.builders.TransactionBuilderApi

trait TransactionBuilderApiModule {
  def transactionBuilderApi(networkId: Int, ledgerId: Int) =
    TransactionBuilderApi.make[IO](
      networkId,
      ledgerId
    )
}
