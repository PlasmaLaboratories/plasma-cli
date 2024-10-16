package xyz.stratalab.strata.cli.mockbase

import xyz.stratalab.sdk.dataApi.IndexerQueryAlgebra
import xyz.stratalab.sdk.models.LockAddress
import xyz.stratalab.indexer.services.{Txo, TxoState}

class BaseIndexerQueryAlgebra[F[_]] extends IndexerQueryAlgebra[F] {

  override def queryUtxo(
      fromAddress: LockAddress,
      txoState: TxoState
  ): F[Seq[Txo]] = ???

}
