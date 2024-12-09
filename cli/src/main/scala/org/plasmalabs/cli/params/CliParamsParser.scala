package org.plasmalabs.cli.params

import org.plasmalabs.cli.params.mode.*
import org.plasmalabs.cli.params.models.CliParams
import scopt.{OParser, OParserBuilder}

object CliParamsParser
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

  val builder: OParserBuilder[CliParams] = OParser.builder[CliParams]

  val paramParser: OParser[Unit, CliParams] =
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
