package xyz.stratalab.strata.cli

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import xyz.stratalab.strata.cli.modules.NodeQueryModeModule
import xyz.stratalab.strata.cli.modules.TemplateModeModule
import xyz.stratalab.strata.cli.modules.IndexerQueryModeModule
import xyz.stratalab.strata.cli.modules.FellowshipsModeModule
import xyz.stratalab.strata.cli.modules.SimpleTransactionModeModule
import xyz.stratalab.strata.cli.modules.TxModeModule
import xyz.stratalab.strata.cli.modules.WalletModeModule
import scopt.OParser
import xyz.stratalab.strata.cli.modules.SimpleMintingModeModule
import xyz.stratalab.strata.cli.modules.ServerModule

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
