package org.plasmalabs.plasma.cli.modules

import cats.effect.IO
import org.plasmalabs.sdk.builders.TransactionBuilderApi

trait TransactionBuilderApiModule {
  def transactionBuilderApi(networkId: Int, ledgerId: Int) =
    TransactionBuilderApi.make[IO](
      networkId,
      ledgerId
    )
}
