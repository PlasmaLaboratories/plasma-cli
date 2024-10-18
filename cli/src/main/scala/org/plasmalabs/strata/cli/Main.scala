package org.plasmalabs.strata.cli

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import org.plasmalabs.strata.cli.modules.NodeQueryModeModule
import org.plasmalabs.strata.cli.modules.TemplateModeModule
import org.plasmalabs.strata.cli.modules.IndexerQueryModeModule
import org.plasmalabs.strata.cli.modules.FellowshipsModeModule
import org.plasmalabs.strata.cli.modules.SimpleTransactionModeModule
import org.plasmalabs.strata.cli.modules.TxModeModule
import org.plasmalabs.strata.cli.modules.WalletModeModule
import scopt.OParser
import org.plasmalabs.strata.cli.modules.SimpleMintingModeModule
import org.plasmalabs.strata.cli.modules.ServerModule

object Main
    extends IOApp
    with IndexerQueryModeModule
    with NodeQueryModeModule
    with TemplateModeModule
    with FellowshipsModeModule
    with WalletModeModule
    with SimpleTransactionModeModule
    with TxModeModule
    with SimpleMintingModeModule
    with ServerModule {

  import StrataCliParamsParserModule._

  override def run(args: List[String]): IO[ExitCode] = {
    OParser.runParser(paramParser, args, StrataCliParams()) match {
      case (Some(params), effects) =>
        val op: IO[Either[String, String]] =
          params.mode match {
            case StrataCliMode.tx =>
              txModeSubcmds(params)
            case StrataCliMode.templates =>
              templateModeSubcmds(params)
            case StrataCliMode.fellowships =>
              fellowshipsModeSubcmds(params)
            case StrataCliMode.wallet =>
              walletModeSubcmds(params)
            case StrataCliMode.simpletransaction =>
              simpleTransactionSubcmds(params)
            case StrataCliMode.simpleminting =>
              simpleMintingSubcmds(params)
            case StrataCliMode.indexerquery =>
              indexerQuerySubcmd(params)
            case StrataCliMode.nodequery =>
              nodeQuerySubcmd(params)
            case StrataCliMode.server =>
              serverSubcmd(params)
            case _ =>
              IO(OParser.runEffects(effects)) >> IO.pure(Left("Invalid mode"))
          }
        import cats.implicits._
        for {
          output <- op
          res <- output.fold(
            x => IO.consoleForIO.errorln(x).map(_ => ExitCode.Error),
            x => IO.consoleForIO.println(x).map(_ => ExitCode.Success)
          )
        } yield res
      case (None, effects) =>
        IO(OParser.runEffects(effects.reverse.tail.reverse)) >> IO.pure(
          ExitCode.Error
        )
    }
  }

}
