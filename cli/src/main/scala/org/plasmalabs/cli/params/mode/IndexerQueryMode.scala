package org.plasmalabs.cli.params.mode

import org.plasmalabs.cli.params.models.*
import scopt.OParser

trait IndexerQueryMode extends Coordinates with Args:

  def indexerQueryMode: OParser[Unit, CliParams] =
    builder
      .cmd("indexer-query")
      .action((_, c) => c.copy(mode = CliMode.indexerquery))
      .text("Indexer query mode")
      .children(
        builder
          .cmd("utxo-by-address")
          .action((_, c) => c.copy(subcmd = CliSubCmd.utxobyaddress))
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
