package org.plasmalabs.cli

import org.plasmalabs.cli.params.mode.*
import scopt.{OParser, OParserBuilder}

object PlasmaCliParamsParserModule
    extends HelpMode
    with ServerMode
    with SimpleTransactionMode
    with Coordinates
    with TemplatesMode
    with FellowshipsMode
    with IndexerQueryMode
    with NodeQueryMode
    with WalletMode
    with TransactionMode
    with SimpleMintingMode {

  val builder: OParserBuilder[PlasmaCliParams] = OParser.builder[PlasmaCliParams]

  val paramParser: OParser[Unit, PlasmaCliParams] =
    OParser.sequence(
      templatesMode,
      fellowshipsMode,
      indexerQueryMode,
      nodeQueryMode,
      walletMode,
      transactionMode,
      simpleTransactionMode,
      simpleMintingMode,
      serverMode,
      helpMode
    )
}
