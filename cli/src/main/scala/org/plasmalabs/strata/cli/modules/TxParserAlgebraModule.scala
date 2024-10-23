package org.plasmalabs.plasma.cli.modules

import cats.effect.IO
import org.plasmalabs.plasma.cli.impl.TxParserAlgebra

trait TxParserAlgebraModule extends TransactionBuilderApiModule {

  def txParserAlgebra(networkId: Int, ledgerId: Int) =
    TxParserAlgebra.make[IO](transactionBuilderApi(networkId, ledgerId))

}
