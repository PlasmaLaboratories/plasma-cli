package org.plasmalabs.cli.params.mode

import org.plasmalabs.cli.PlasmaCliParamsParserModule.{fellowshipNameArg, walletDbArg}
import org.plasmalabs.cli.{PlasmaCliMode, PlasmaCliParams, PlasmaCliSubCmd}
import scopt.OParser

trait FellowshipsMode extends Args:

  def fellowshipsMode: OParser[Unit, PlasmaCliParams] =
    builder
      .cmd("fellowships")
      .action((_, c) => c.copy(mode = PlasmaCliMode.fellowships))
      .text("Fellowship mode")
      .children(
        builder
          .cmd("list")
          .action((_, c) => c.copy(subcmd = PlasmaCliSubCmd.list))
          .text("List existing fellowships")
          .children(
            walletDbArg
          ),
        builder
          .cmd("add")
          .action((_, c) => c.copy(subcmd = PlasmaCliSubCmd.add))
          .text("Add a new fellowships")
          .children(
            Seq(
              walletDbArg,
              fellowshipNameArg
            ): _*
          )
      )

end FellowshipsMode
