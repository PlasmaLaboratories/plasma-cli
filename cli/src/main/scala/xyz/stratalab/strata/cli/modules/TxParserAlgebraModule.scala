package org.plasmalabs.strata.cli.modules

import cats.effect.IO
import org.plasmalabs.strata.cli.impl.TxParserAlgebra

trait TxParserAlgebraModule extends TransactionBuilderApiModule {

  def txParserAlgebra(networkId: Int, ledgerId: Int) =
    TxParserAlgebra.make[IO](transactionBuilderApi(networkId, ledgerId))

}
