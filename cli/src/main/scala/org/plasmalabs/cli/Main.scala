package org.plasmalabs.cli

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import org.plasmalabs.cli.modules.NodeQueryModeModule
import org.plasmalabs.cli.modules.TemplateModeModule
import org.plasmalabs.cli.modules.IndexerQueryModeModule
import org.plasmalabs.cli.modules.FellowshipsModeModule
import org.plasmalabs.cli.modules.SimpleTransactionModeModule
import org.plasmalabs.cli.modules.TxModeModule
import org.plasmalabs.cli.modules.WalletModeModule
import scopt.OParser
import org.plasmalabs.cli.modules.SimpleMintingModeModule
import org.plasmalabs.cli.modules.ServerModule

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

  import PlasmaCliParamsParserModule._

  override def run(args: List[String]): IO[ExitCode] =
    OParser.runParser(paramParser, args, PlasmaCliParams()) match {
      case (Some(params), effects) =>
        val op: IO[Either[String, String]] =
          params.mode match {
            case PlasmaCliMode.tx =>
              txModeSubcmds(params)
            case PlasmaCliMode.templates =>
              templateModeSubcmds(params)
            case PlasmaCliMode.fellowships =>
              fellowshipsModeSubcmds(params)
            case PlasmaCliMode.wallet =>
              walletModeSubcmds(params)
            case PlasmaCliMode.simpletransaction =>
              simpleTransactionSubcmds(params)
            case PlasmaCliMode.simpleminting =>
              simpleMintingSubcmds(params)
            case PlasmaCliMode.indexerquery =>
              indexerQuerySubcmd(params)
            case PlasmaCliMode.nodequery =>
              nodeQuerySubcmd(params)
            case PlasmaCliMode.server =>
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
