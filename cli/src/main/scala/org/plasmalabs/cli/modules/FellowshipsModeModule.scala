package org.plasmalabs.cli.modules

import cats.effect.IO
import org.plasmalabs.cli.controllers.FellowshipsController
import org.plasmalabs.cli.params.CliParamsParser
import org.plasmalabs.cli.params.models.*
import org.plasmalabs.sdk.servicekit.{FellowshipStorageApi, WalletStateResource}
import scopt.OParser

trait FellowshipsModeModule extends WalletStateResource {

  def fellowshipsModeSubcmds(
    validateParams: CliParams
  ): IO[Either[String, String]] = {
    val fellowshipStorageAlgebra = FellowshipStorageApi.make[IO](
      walletResource(validateParams.walletFile)
    )
    validateParams.subcmd match {
      case CliSubCmd.invalid =>
        IO.pure(
          Left(
            OParser.usage(
              CliParamsParser.fellowshipsMode
            ) + "\nA subcommand needs to be specified"
          )
        )
      case CliSubCmd.add =>
        new FellowshipsController(fellowshipStorageAlgebra)
          .addFellowship(validateParams.fellowshipName)
      case CliSubCmd.list =>
        new FellowshipsController(fellowshipStorageAlgebra)
          .listFellowships()
    }
  }
}
