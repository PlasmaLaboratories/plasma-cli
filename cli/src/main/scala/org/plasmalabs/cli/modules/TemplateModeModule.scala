package org.plasmalabs.cli.modules

import cats.effect.IO
import org.plasmalabs.cli.controllers.TemplatesController
import org.plasmalabs.cli.params.CliParamsParser
import org.plasmalabs.cli.params.models.*
import org.plasmalabs.sdk.servicekit.{TemplateStorageApi, WalletStateResource}
import scopt.OParser

trait TemplateModeModule extends WalletStateResource {

  def templateModeSubcmds(
    validateParams: CliParams
  ): IO[Either[String, String]] = {
    val templateStorageAlgebra = TemplateStorageApi.make[IO](
      walletResource(validateParams.walletFile)
    )
    validateParams.subcmd match {
      case CliSubCmd.invalid =>
        IO.pure(
          Left(
            OParser.usage(
              CliParamsParser.templatesMode
            ) + "\nA subcommand needs to be specified"
          )
        )
      case CliSubCmd.list =>
        new TemplatesController(
          templateStorageAlgebra
        )
          .listTemplates()
      case CliSubCmd.add =>
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
