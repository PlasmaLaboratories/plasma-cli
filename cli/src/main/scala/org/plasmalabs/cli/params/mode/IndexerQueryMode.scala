package org.plasmalabs.cli.params.mode

import org.plasmalabs.cli.{PlasmaCliMode, PlasmaCliParams, PlasmaCliSubCmd}
import scopt.OParser

trait IndexerQueryMode extends Coordinates with Args:

  def indexerQueryMode: OParser[Unit, PlasmaCliParams] =
    builder
      .cmd("indexer-query")
      .action((_, c) => c.copy(mode = PlasmaCliMode.indexerquery))
      .text("Indexer query mode")
      .children(
        builder
          .cmd("utxo-by-address")
          .action((_, c) => c.copy(subcmd = PlasmaCliSubCmd.utxobyaddress))
          .text("Query utxo")
          .children(
            (coordinates ++ hostPort ++ Seq(
              fromAddress,
              walletDbArg,
              tokenType.optional()
            )): _*
          )
      )

end IndexerQueryMode
