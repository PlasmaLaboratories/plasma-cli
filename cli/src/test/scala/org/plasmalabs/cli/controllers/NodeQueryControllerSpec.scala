package org.plasmalabs.cli.controllers

import cats.effect.IO
import munit.CatsEffectSuite
import org.plasmalabs.cli.mockbase.BaseNodeQueryAlgebra
import org.plasmalabs.cli.modules.DummyObjects
import org.plasmalabs.cli.views.BlockDisplayOps
import org.plasmalabs.consensus.models.{BlockHeader, BlockId}
import org.plasmalabs.node.models.BlockBody
import org.plasmalabs.sdk.display.DisplayOps.DisplayTOps
import org.plasmalabs.sdk.display.transactionDisplay
import org.plasmalabs.sdk.models.TransactionId
import org.plasmalabs.sdk.models.transaction.IoTransaction

class NodeQueryControllerSpec extends CatsEffectSuite with DummyObjects {

  test("blockByHeight should return error when block is not there") {
    val nodeQueryController = new NodeQueryController[IO](
      new BaseNodeQueryAlgebra[IO] {

        override def blockByHeight(
          height: Long
        ): IO[Option[(BlockId, BlockHeader, BlockBody, Seq[IoTransaction])]] =
          IO(None)

      }
    )
    nodeQueryController
      .blockByHeight(1)
      .assertEquals(
        Left("No blocks found at that height")
      )
  }

  test("blockByHeight should display a block when it is there") {
    val nodeQueryController = new NodeQueryController[IO](
      new BaseNodeQueryAlgebra[IO] {

        override def blockByHeight(
          height: Long
        ): IO[Option[(BlockId, BlockHeader, BlockBody, Seq[IoTransaction])]] =
          IO(
            Some((blockId01, blockHeader01, blockBody01, Seq(iotransaction01)))
          )

      }
    )
    nodeQueryController
      .blockByHeight(1)
      .assertEquals(
        Right(BlockDisplayOps.display(blockId01, Seq(iotransaction01)))
      )
  }

  test("blockById should return error when block is not there") {
    val nodeQueryController = new NodeQueryController[IO](
      new BaseNodeQueryAlgebra[IO] {

        override def blockById(
          blockId: BlockId
        ): IO[Option[(BlockId, BlockHeader, BlockBody, Seq[IoTransaction])]] =
          IO(None)

      }
    )
    nodeQueryController
      .blockById("A7k6tpK25N5ZvmjkYn8jN6CnP8u9aNheT9cYb7ZjS3PN")
      .assertEquals(
        Left("No blocks found at that block id")
      )
  }

  test("blockById should display a block when it is there") {
    val nodeQueryController = new NodeQueryController[IO](
      new BaseNodeQueryAlgebra[IO] {

        override def blockById(
          blockId: BlockId
        ): IO[Option[(BlockId, BlockHeader, BlockBody, Seq[IoTransaction])]] =
          IO(
            Some((blockId01, blockHeader01, blockBody01, Seq(iotransaction01)))
          )

      }
    )
    nodeQueryController
      .blockById("A7k6tpK25N5ZvmjkYn8jN6CnP8u9aNheT9cYb7ZjS3PN")
      .assertEquals(
        Right(BlockDisplayOps.display(blockId01, Seq(iotransaction01)))
      )
  }

  test("fetchTransaction should return error when transaction is not there") {
    val nodeQueryController = new NodeQueryController[IO](
      new BaseNodeQueryAlgebra[IO] {

        override def fetchTransaction(
          txId: TransactionId
        ): IO[Option[IoTransaction]] = IO(None)

      }
    )
    nodeQueryController
      .fetchTransaction("A7k6tpK25N5ZvmjkYn8jN6CnP8u9aNheT9cYb7ZjS3PN")
      .assertEquals(
        Left(
          "No transaction found with id A7k6tpK25N5ZvmjkYn8jN6CnP8u9aNheT9cYb7ZjS3PN"
        )
      )
  }

  test("fetchTransaction should display a transaction when it is there") {
    val nodeQueryController = new NodeQueryController[IO](
      new BaseNodeQueryAlgebra[IO] {

        override def fetchTransaction(
          txId: TransactionId
        ): IO[Option[IoTransaction]] = IO(Some(iotransaction01))

      }
    )
    nodeQueryController
      .fetchTransaction("A7k6tpK25N5ZvmjkYn8jN6CnP8u9aNheT9cYb7ZjS3PN")
      .assertEquals(
        Right(iotransaction01.display)
      )
  }

  test("makeBlocks should do something") {
    val nodeQueryController = new NodeQueryController[IO](
      new BaseNodeQueryAlgebra[IO] {

        override def makeBlocks(
          nbOfBlocks: Int
        ): IO[Unit] =
          IO(Right("Block(s) created successfully"))

      }
    )
    nodeQueryController
      .makeBlocks(1)
      .assertEquals(
        Right("Block(s) created successfully")
      )
  }

}
