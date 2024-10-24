package org.plasmalabs.plasma.cli.controllers

import cats.Id
import cats.data.Validated
import cats.effect.kernel.Sync
import org.plasmalabs.sdk.dataApi.{TemplateStorageAlgebra, WalletTemplate}
import org.plasmalabs.plasma.cli.impl.QuivrFastParser
import org.plasmalabs.sdk.codecs.LockTemplateCodecs

class TemplatesController[F[_]: Sync](
    templateStorageAlgebra: TemplateStorageAlgebra[F]
) {

  def addTemplate(
      name: String,
      lockTemplate: String
  ): F[Either[String, String]] = {
    import cats.implicits._
    for {
      lockTemplateStruct <- Sync[F].delay(
        QuivrFastParser.make[Id].parseQuivr(lockTemplate)
      )
      res <- lockTemplateStruct match {
        case Validated.Valid(lockTemplate) =>
          for {
            lockTemplateAsJson <-
              Sync[F].delay(
                LockTemplateCodecs.encodeLockTemplate[Id](
                  lockTemplate
                )
              )
            added <- templateStorageAlgebra.addTemplate(
              WalletTemplate(0, name, lockTemplateAsJson.noSpaces)
            )
          } yield
            if (added == 1) Right("Template added successfully")
            else Left("Failed to add template")
        case Validated.Invalid(e) =>
          import cats.implicits._

          e.toList
            .traverse(x => Sync[F].delay(s"Error at ${x.location}: ${x.error}"))
            .map(x => Left(x.mkString("\n")))
      }
    } yield res
  }

  def listTemplates(): F[Either[String, String]] = {
    import org.plasmalabs.plasma.cli.views.WalletModelDisplayOps._
    import cats.implicits._

    templateStorageAlgebra
      .findTemplates()
      .map(templates =>
        Right(
          displayWalletTemplateHeader() + "\n" + templates
            .map(display)
            .mkString("\n")
        )
      )
  }
}
