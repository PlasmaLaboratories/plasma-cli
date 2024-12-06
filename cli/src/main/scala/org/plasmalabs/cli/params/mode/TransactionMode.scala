package org.plasmalabs.cli.params.mode

import org.plasmalabs.cli.{PlasmaCliMode, PlasmaCliParams, PlasmaCliSubCmd}
import scopt.{OParser, OParserBuilder}

trait TransactionMode extends Args:

  val builder: OParserBuilder[PlasmaCliParams]

  import builder._

  def transactionMode: OParser[Unit, PlasmaCliParams] =
    cmd("tx")
      .action((_, c) => c.copy(mode = PlasmaCliMode.tx))
      .text("Transaction mode")
      .children(
        cmd("inspect")
          .action((_, c) => c.copy(subcmd = PlasmaCliSubCmd.inspect))
          .text("Inspect transaction")
          .children(
            inputFileArg
          ),
        cmd("broadcast")
          .action((_, c) => c.copy(subcmd = PlasmaCliSubCmd.broadcast))
          .text("Broadcast transaction")
          .children(
            ((hostPortNetwork ++ Seq(
              inputFileArg.required()
            ))): _*
          ),
        cmd("prove")
          .action((_, c) => c.copy(subcmd = PlasmaCliSubCmd.prove))
          .text("Prove transaction")
          .children(
            ((keyfileAndPassword ++ Seq(
              walletDbArg,
              outputArg.required(),
              inputFileArg.required()
            ))): _*
          ),
        cmd("create")
          .action((_, c) => c.copy(subcmd = PlasmaCliSubCmd.create))
          .text("Create transaction")
          .children(
            ((hostPortNetwork ++ Seq(
              outputArg,
              inputFileArg
            ))): _*
          )
      )

end TransactionMode
