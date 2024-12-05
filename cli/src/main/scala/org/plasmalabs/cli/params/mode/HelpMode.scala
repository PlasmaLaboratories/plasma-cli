package org.plasmalabs.cli.params.mode

import org.plasmalabs.cli.{PlasmaCliMode, PlasmaCliParams}
import scopt.{OParser, OParserBuilder}

trait HelpMode:

  given builder: OParserBuilder[PlasmaCliParams]

  def helpMode: OParser[Unit, PlasmaCliParams] =
    builder
      .cmd("help")
      .action((_, c) => c.copy(mode = PlasmaCliMode.help))
      .text("""Welcome to Plasma-Cli
          |Valid modes are:
          |   - templates: Template mode
          |   - fellowships: Fellowship mode
          |   - indexer-query: Indexer query mode
          |   - node-query: Node query mode
          |   - wallet: Wallet mode
          |   - tx: Transaction mode
          |   - simple-transaction: Simple transaction mode
          |   - simple-minting: Simple minting mode
          |   - server: Server mode
          |""".stripMargin)

end HelpMode
