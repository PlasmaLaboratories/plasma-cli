package org.plasmalabs.cli.params.mode

import org.plasmalabs.cli.params.CliParamsParser.{fellowshipNameArg, walletDbArg}
import org.plasmalabs.cli.params.models.*
import scopt.OParser

trait FellowshipsMode extends Args:

  def fellowshipsMode: OParser[Unit, CliParams] =
    builder
      .cmd("fellowships")
      .action((_, c) => c.copy(mode = CliMode.fellowships))
      .text("Fellowship mode")
      .children(
        builder
          .cmd("list")
          .action((_, c) => c.copy(subcmd = CliSubCmd.list))
          .text("List existing fellowships")
          .children(
            walletDbArg
          ),
        builder
          .cmd("add")
          .action((_, c) => c.copy(subcmd = CliSubCmd.add))
          .text("Add a new fellowships")
          .children(
            Seq(
              walletDbArg,
              fellowshipNameArg
            ): _*
          )
      )

end FellowshipsMode
