package org.plasmalabs.cli

import cats.effect.{ExitCode, IO, IOApp}
import org.plasmalabs.cli.modules.{
  FellowshipsModeModule,
  IndexerQueryModeModule,
  NodeQueryModeModule,
  ServerModule,
  SimpleMintingModeModule,
  SimpleTransactionModeModule,
  TemplateModeModule,
  TxModeModule,
  WalletModeModule
}
import org.plasmalabs.cli.params.CliParamsParser
import org.plasmalabs.cli.params.models.*
import scopt.OParser

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

  import org.plasmalabs.cli.params.CliParamsParser._

  override def run(args: List[String]): IO[ExitCode] =
    OParser.runParser(paramParser, args, CliParams()) match {
      case (Some(params), effects) =>
        val op: IO[Either[String, String]] =
          params.mode match {
            case CliMode.tx =>
              txModeSubcmds(params)
            case CliMode.templates =>
              templateModeSubcmds(params)
            case CliMode.fellowships =>
              fellowshipsModeSubcmds(params)
            case CliMode.wallet =>
              walletModeSubcmds(params)
            case CliMode.simpletransaction =>
              simpleTransactionSubcmds(params)
            case CliMode.simpleminting =>
              simpleMintingSubcmds(params)
            case CliMode.indexerquery =>
              indexerQuerySubcmd(params)
            case CliMode.nodequery =>
              nodeQuerySubcmd(params)
            case CliMode.server =>
              serverSubcmd(params)
            case CliMode.help =>
              IO.pure(Right(OParser.usage(CliParamsParser.helpMode)))
            case _ =>
              IO(OParser.runEffects(effects)) >> IO.pure(Left("Invalid mode, try 'help' for more information"))
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
