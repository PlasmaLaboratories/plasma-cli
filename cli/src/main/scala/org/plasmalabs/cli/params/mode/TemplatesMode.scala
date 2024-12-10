package org.plasmalabs.cli.params.mode

import org.plasmalabs.cli.params.CliParamsParser.{templateNameArg, walletDbArg}
import org.plasmalabs.cli.params.models.*
import scopt.OParser

trait TemplatesMode extends Args:

  def templatesMode: OParser[Unit, CliParams] =
    builder
      .cmd("templates")
      .action((_, c) => c.copy(mode = CliMode.templates))
      .text("Template mode")
      .children(
        builder
          .cmd("list")
          .action((_, c) => c.copy(subcmd = CliSubCmd.list))
          .text("List existing templates")
          .children(
            walletDbArg
          ),
        builder
          .cmd("add")
          .action((_, c) => c.copy(subcmd = CliSubCmd.add))
          .text("Add a new templates")
          .children(
            walletDbArg,
            templateNameArg,
            builder
              .opt[String]("lock-template")
              .validate(x =>
                if (x.trim().isEmpty)
                  builder.failure("Template template may not be empty")
                else builder.success
              )
              .action((x, c) => c.copy(lockTemplate = x))
              .text("Template template. (mandatory)")
              .required()
          )
      )

end TemplatesMode
