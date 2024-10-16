package xyz.stratalab.strata.cli.mockbase

import xyz.stratalab.sdk.dataApi.NodeQueryAlgebra
import xyz.stratalab.sdk.models.TransactionId
import xyz.stratalab.sdk.models.transaction.IoTransaction
import xyz.stratalab.consensus.models.BlockId
import xyz.stratalab.node.models.BlockBody
import xyz.stratalab.consensus.models.BlockHeader
import xyz.stratalab.node.services.SynchronizationTraversalRes

abstract class BaseNodeQueryAlgebra[F[_]] extends NodeQueryAlgebra[F] {

  override def synchronizationTraversal()
      : F[Iterator[SynchronizationTraversalRes]] = ???

  override def makeBlock(nbOfBlocks: Int): F[Unit] = ???

  override def blockByHeight(
      height: Long
  ): F[Option[(BlockId, BlockHeader, BlockBody, Seq[IoTransaction])]] = ???

  override def blockById(
      blockId: BlockId
  ): F[Option[(BlockId, BlockHeader, BlockBody, Seq[IoTransaction])]] = ???

  override def blockByDepth(
      depth: Long
  ): F[Option[(BlockId, BlockHeader, BlockBody, Seq[IoTransaction])]] = ???

  override def fetchTransaction(
      txId: TransactionId
  ): F[Option[IoTransaction]] = ???

  override def broadcastTransaction(tx: IoTransaction): F[TransactionId] = ???
}
