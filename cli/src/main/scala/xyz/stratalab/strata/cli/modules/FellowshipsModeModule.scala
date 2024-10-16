package xyz.stratalab.strata.cli.modules

import cats.effect.IO
import xyz.stratalab.strata.cli.controllers.FellowshipsController
import xyz.stratalab.sdk.servicekit.{FellowshipStorageApi, WalletStateResource}
import xyz.stratalab.strata.cli.StrataCliSubCmd
import xyz.stratalab.strata.cli.StrataCliParams
import scopt.OParser
import xyz.stratalab.strata.cli.StrataCliParamsParserModule

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
