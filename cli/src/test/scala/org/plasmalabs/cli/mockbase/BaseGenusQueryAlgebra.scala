package org.plasmalabs.cli.mockbase

import org.plasmalabs.indexer.services.{Txo, TxoState}
import org.plasmalabs.sdk.dataApi.IndexerQueryAlgebra
import org.plasmalabs.sdk.models.LockAddress

class BaseIndexerQueryAlgebra[F[_]] extends IndexerQueryAlgebra[F] {

  override def queryUtxo(
    fromAddress: LockAddress,
    txoState:    TxoState
  ): F[Seq[Txo]] = ???

}
