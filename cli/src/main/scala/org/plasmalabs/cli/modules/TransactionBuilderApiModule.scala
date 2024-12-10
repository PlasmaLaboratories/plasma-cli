package org.plasmalabs.cli.modules

import cats.effect.IO
import org.plasmalabs.sdk.builders.TransactionBuilderApi

trait TransactionBuilderApiModule {

  def transactionBuilderApi(networkId: Int, ledgerId: Int): TransactionBuilderApi[IO] =
    TransactionBuilderApi.make[IO](
      networkId,
      ledgerId
    )
}
