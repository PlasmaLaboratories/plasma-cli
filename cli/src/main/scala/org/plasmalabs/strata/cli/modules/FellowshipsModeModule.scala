package org.plasmalabs.plasma.cli.modules

import cats.effect.IO
import org.plasmalabs.plasma.cli.controllers.FellowshipsController
import org.plasmalabs.sdk.servicekit.{FellowshipStorageApi, WalletStateResource}
import org.plasmalabs.plasma.cli.StrataCliSubCmd
import org.plasmalabs.plasma.cli.StrataCliParams
import scopt.OParser
import org.plasmalabs.plasma.cli.StrataCliParamsParserModule

trait FellowshipsModeModule extends WalletStateResource {
  def fellowshipsModeSubcmds(
      validateParams: StrataCliParams
  ): IO[Either[String, String]] = {
    val fellowshipStorageAlgebra = FellowshipStorageApi.make[IO](
      walletResource(validateParams.walletFile)
    )
    validateParams.subcmd match {
      case StrataCliSubCmd.invalid =>
        IO.pure(
          Left(
            OParser.usage(
              StrataCliParamsParserModule.fellowshipsMode
            ) + "\nA subcommand needs to be specified"
          )
        )
      case StrataCliSubCmd.add =>
        new FellowshipsController(fellowshipStorageAlgebra)
          .addFellowship(validateParams.fellowshipName)
      case StrataCliSubCmd.list =>
        new FellowshipsController(fellowshipStorageAlgebra)
          .listFellowships()
    }
  }
}
