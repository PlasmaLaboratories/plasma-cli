package org.plasmalabs.cli.modules

import cats.effect.IO
import org.plasmalabs.cli.controllers.TemplatesController
import org.plasmalabs.sdk.servicekit.{TemplateStorageApi, WalletStateResource}
import org.plasmalabs.cli.PlasmaCliSubCmd
import org.plasmalabs.cli.PlasmaCliParams
import scopt.OParser
import org.plasmalabs.cli.PlasmaCliParamsParserModule

trait TemplateModeModule extends WalletStateResource {
  def templateModeSubcmds(
      validateParams: PlasmaCliParams
  ): IO[Either[String, String]] = {
    val templateStorageAlgebra = TemplateStorageApi.make[IO](
      walletResource(validateParams.walletFile)
    )
    validateParams.subcmd match {
      case PlasmaCliSubCmd.invalid =>
        IO.pure(
          Left(
            OParser.usage(
              PlasmaCliParamsParserModule.templatesMode
            ) + "\nA subcommand needs to be specified"
          )
        )
      case PlasmaCliSubCmd.list =>
        new TemplatesController(
          templateStorageAlgebra
        )
          .listTemplates()
      case PlasmaCliSubCmd.add =>
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
