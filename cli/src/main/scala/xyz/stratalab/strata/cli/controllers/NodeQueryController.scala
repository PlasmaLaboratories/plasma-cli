package xyz.stratalab.strata.cli.controllers

import cats.effect.kernel.Sync
import xyz.stratalab.strata.cli.views.BlockDisplayOps
import xyz.stratalab.sdk.dataApi.NodeQueryAlgebra
import xyz.stratalab.sdk.display.DisplayOps.DisplayTOps
import xyz.stratalab.sdk.models.TransactionId
import xyz.stratalab.sdk.utils.Encoding
import xyz.stratalab.consensus.models.BlockId
import com.google.protobuf.ByteString

class NodeQueryController[F[_]: Sync](
    nodeQueryAlgebra: NodeQueryAlgebra[F]
) {

  def makeBlock(
      nbOfBlocks: Int
  ): F[Either[String, String]] = {
    import cats.implicits._
    nodeQueryAlgebra.makeBlock(nbOfBlocks).map { _ =>
      "Block(s) created successfully".asRight[String]
    }
  }

  def blockByHeight(
      height: Long
  ): F[Either[String, String]] = {
    import cats.implicits._
    nodeQueryAlgebra
      .blockByHeight(
        height
      )
      .map { someResult =>
        someResult match {
          case Some(((blockId, _, _, ioTransactions))) =>
            Right(BlockDisplayOps.display(blockId, ioTransactions))
          case None =>
            Left("No blocks found at that height")
        }
      }
      .attempt
      .map {
        _ match {
          case Left(e) =>
            e.printStackTrace()
            Left("Problem contacting the network.")
          case Right(txos) => txos
        }
      }
  }

  def blockById(
      pBlockId: String
  ): F[Either[String, String]] = {
    import cats.implicits._
    nodeQueryAlgebra
      .blockById(
        Encoding
          .decodeFromBase58(pBlockId)
          .map(x => BlockId(ByteString.copyFrom(x)))
          .toOption // validation should ensure that this is a Some
          .get
      )
      .map { someResult =>
        someResult match {
          case Some(((blockId, _, _, ioTransactions))) =>
            Right(BlockDisplayOps.display(blockId, ioTransactions))
          case None =>
            Left("No blocks found at that block id")
        }
      }
      .attempt
      .map {
        _ match {
          case Left(_)     => Left("Problem contacting the network.")
          case Right(txos) => txos
        }
      }
  }

  def fetchTransaction(transactionId: String): F[Either[String, String]] = {
    import cats.implicits._
    nodeQueryAlgebra
      .fetchTransaction(
        Encoding
          .decodeFromBase58(transactionId)
          .map(x => TransactionId(ByteString.copyFrom(x)))
          .toOption // validation should ensure that this is a Some
          .get
      )
      .map { someResult =>
        someResult match {
          case Some(ioTransaction) =>
            Right(ioTransaction.display)
          case None =>
            Left(s"No transaction found with id ${transactionId}")
        }
      }
      .attempt
      .map {
        _ match {
          case Left(_)     => Left("Problem contacting the network.")
          case Right(txos) => txos
        }
      }
  }

}
