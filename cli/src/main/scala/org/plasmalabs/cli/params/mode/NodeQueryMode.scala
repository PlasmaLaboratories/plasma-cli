package org.plasmalabs.cli.params.mode

import org.plasmalabs.cli.{PlasmaCliMode, PlasmaCliParams, PlasmaCliSubCmd}
import org.plasmalabs.sdk.utils.Encoding
import scopt.OParser

trait NodeQueryMode extends Args:

  import builder._

  def nodeQueryMode: OParser[Unit, PlasmaCliParams] =
    cmd("node-query")
      .action((_, c) => c.copy(mode = PlasmaCliMode.nodequery))
      .text("Node query mode")
      .children(
        cmd("mint-block")
          .action((_, c) => c.copy(subcmd = PlasmaCliSubCmd.mintblock))
          .text("Mint given number of blocks")
          .children(
            (hostPort ++ Seq(
              opt[Int]("nb-blocks")
                .action((x, c) => c.copy(nbOfBlocks = x))
                .text("The number of blocks to mint. (mandatory")
                .validate(x =>
                  if (x >= 0) success
                  else failure("Number of blocks must be bigger or equal to 1")
                )
            )): _*
          ),
        cmd("block-by-height")
          .action((_, c) => c.copy(subcmd = PlasmaCliSubCmd.blockbyheight))
          .text("Get the block at a given height")
          .children(
            (hostPort ++ Seq(
              opt[Long]("height")
                .action((x, c) => c.copy(height = x))
                .text("The height of the block. (mandatory)")
                .validate(x =>
                  if (x >= 0) success
                  else failure("Height must be greater than or equal to 0")
                )
            )): _*
          ),
        cmd("block-by-id")
          .action((_, c) => c.copy(subcmd = PlasmaCliSubCmd.blockbyid))
          .text("Get the block with a given id")
          .children(
            (hostPort ++ Seq(
              opt[String]("block-id")
                .validate(x =>
                  Encoding.decodeFromBase58(x) match {
                    case Left(_)  => failure("Invalid block id")
                    case Right(_) => success
                  }
                )
                .action((x, c) => c.copy(blockId = x))
                .text("The id of the block in base 58. (mandatory)")
            )): _*
          ),
        cmd("transaction-by-id")
          .action((_, c) => c.copy(subcmd = PlasmaCliSubCmd.transactionbyid))
          .text("Get the transaction with a given id")
          .children(
            (hostPort ++ Seq(
              opt[String]("transaction-id")
                .validate(x =>
                  Encoding.decodeFromBase58(x) match {
                    case Left(_)  => failure("Invalid transaction id")
                    case Right(_) => success
                  }
                )
                .action((x, c) => c.copy(transactionId = x))
                .text("The id of the transaction in base 58. (mandatory)")
            )): _*
          )
      )
end NodeQueryMode
