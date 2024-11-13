package org.plasmalabs.cli.modules

import cats.effect.IO
import org.plasmalabs.cli.controllers.TemplatesController
import org.plasmalabs.sdk.servicekit.{TemplateStorageApi, WalletStateResource}
import org.plasmalabs.cli.StrataCliSubCmd
import org.plasmalabs.cli.StrataCliParams
import scopt.OParser
import org.plasmalabs.cli.StrataCliParamsParserModule

trait TemplateModeModule extends WalletStateResource {
  def templateModeSubcmds(
      validateParams: StrataCliParams
  ): IO[Either[String, String]] = {
    val templateStorageAlgebra = TemplateStorageApi.make[IO](
      walletResource(validateParams.walletFile)
    )
    validateParams.subcmd match {
      case StrataCliSubCmd.invalid =>
        IO.pure(
          Left(
            OParser.usage(
              StrataCliParamsParserModule.templatesMode
            ) + "\nA subcommand needs to be specified"
          )
        )
      case StrataCliSubCmd.list =>
        new TemplatesController(
          templateStorageAlgebra
        )
          .listTemplates()
      case StrataCliSubCmd.add =>
        new TemplatesController(
          templateStorageAlgebra
        )
          .addTemplate(
            validateParams.templateName,
            validateParams.lockTemplate
          )
    }
  }
}