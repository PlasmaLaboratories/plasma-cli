package org.plasmalabs.cli.params.models

object CliMode extends Enumeration {
  type CliMode = Value

  val invalid, wallet, indexerquery, nodequery, simpletransaction, simpleminting, fellowships, templates, tx, server,
    help =
    Value
}
