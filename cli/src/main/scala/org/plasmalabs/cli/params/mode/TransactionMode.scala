package org.plasmalabs.cli.params.mode

import org.plasmalabs.cli.params.models.*
import scopt.{OParser, OParserBuilder}

trait TransactionMode extends Args:

  val builder: OParserBuilder[CliParams]

  import builder._

  def transactionMode: OParser[Unit, CliParams] =
    cmd("tx")
      .action((_, c) => c.copy(mode = CliMode.tx))
      .text("Transaction mode")
      .children(
        cmd("inspect")
          .action((_, c) => c.copy(subcmd = CliSubCmd.inspect))
          .text("Inspect transaction")
          .children(
            inputFileArg
          ),
        cmd("broadcast")
          .action((_, c) => c.copy(subcmd = CliSubCmd.broadcast))
          .text("Broadcast transaction")
          .children(
            ((hostPortNetwork ++ Seq(
              inputFileArg.required()
            ))): _*
          ),
        cmd("prove")
          .action((_, c) => c.copy(subcmd = CliSubCmd.prove))
          .text("Prove transaction")
          .children(
            ((keyfileAndPassword ++ Seq(
              walletDbArg,
              outputArg.required(),
              inputFileArg.required()
            ))): _*
          ),
        cmd("create")
          .action((_, c) => c.copy(subcmd = CliSubCmd.create))
          .text("Create transaction")
          .children(
            ((hostPortNetwork ++ Seq(
              outputArg,
              inputFileArg
            ))): _*
          )
      )

end TransactionMode
