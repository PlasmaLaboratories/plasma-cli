package org.plasmalabs.cli.modules

import cats.effect.IO
import org.plasmalabs.cli.impl.TxParserAlgebra

trait TxParserAlgebraModule extends TransactionBuilderApiModule {

  def txParserAlgebra(networkId: Int, ledgerId: Int) =
    TxParserAlgebra.make[IO](transactionBuilderApi(networkId, ledgerId))

}
