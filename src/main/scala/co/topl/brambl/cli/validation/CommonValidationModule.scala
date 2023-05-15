package co.topl.brambl.cli.validation

import scala.util.Try
import cats.data.Validated
import cats.data.ValidatedNel
import co.topl.brambl.cli.BramblCliMode
import co.topl.brambl.cli.BramblCliSubCmd
import co.topl.brambl.cli.NetworkIdentifiers

trait CommonValidationModule {

  def validateMode(mode: String) = {
    Try(BramblCliMode.withName(mode)).toOption match {
      case Some(mode) => Validated.validNel(mode)
      case None =>
        Validated.invalidNel(
          "Invalid mode. Valid values are " + BramblCliMode.values.mkString(
            ", "
          )
        )
    }
  }
  def validateNetwork(network: String) = {
    NetworkIdentifiers.fromString(network) match {
      case Some(mode) => Validated.validNel(mode)
      case None =>
        Validated.invalidNel(
          "Invalid network. Valid values are " + NetworkIdentifiers.values
            .mkString(
              ", "
            )
        )
    }
  }

  def validatePort(port: Int) = {
    import cats.implicits._
    if (port > 0 && port < 65536) {
      port.validNel
    } else {
      "Port must be between 0 and 65536".invalidNel
    }
  }

  def validateHost(host: String) = {
    import cats.implicits._
    if (host.nonEmpty) {
      host.validNel
    } else {
      "Host must be non-empty".invalidNel
    }
  }

  def checkValidSubCmd(
      mode: BramblCliMode.BramblCliMode,
      subcmd: String,
      validSubCmds: Set[BramblCliSubCmd.Value]
  ) = {
    Try(BramblCliSubCmd.withName(subcmd)).toOption match {
      case Some(subcmd) =>
        if (validSubCmds.contains(subcmd)) {
          Validated.validNel(subcmd)
        } else {
          Validated.invalidNel(
            s"Invalid $mode for utxo mode. Valid values are " + validSubCmds
              .mkString(
                ", "
              )
          )
        }
      case None =>
        Validated.invalidNel(
          "Invalid subcmd. Valid values are " + BramblCliSubCmd.values.mkString(
            ", "
          )
        )
    }
  }

  def validateSubCmd(
      mode: BramblCliMode.BramblCliMode,
      subcmd: String
  ): ValidatedNel[String, BramblCliSubCmd.Value] = {
    mode match {
      case BramblCliMode.simpletransaction =>
        checkValidSubCmd(mode, subcmd, Set(BramblCliSubCmd.create))
      case BramblCliMode.wallet =>
        checkValidSubCmd(
          mode,
          subcmd,
          Set(BramblCliSubCmd.init)
        )
      case BramblCliMode.utxo =>
        checkValidSubCmd(
          mode,
          subcmd,
          Set(BramblCliSubCmd.query)
        )
    }
  }

  def validatePassword(password: String) = {
    if (password.trim().length > 0) {
      Validated.validNel(password)
    } else {
      Validated.invalidNel(
        "Password must not be empty"
      )
    }
  }

  def validateOutputfile(
      someOutputFile: Option[String],
      required: Boolean
  ): ValidatedNel[String, Option[String]] = {
    someOutputFile match {
      case Some(outputFile) =>
        if (outputFile.trim().length >= 0) {
          Validated.validNel(Some(outputFile))
        } else {
          Validated.invalidNel(
            "Output file must not be empty"
          )
        }
      case None =>
        if (required) Validated.invalidNel("Output file is required")
        else Validated.validNel(None)
    }
  }
  def validateWalletFile(
      someWalletFile: Option[String]
  ): ValidatedNel[String, String] = {
    someWalletFile match {
      case Some(walletFile) =>
        if (walletFile.trim().length >= 0) {
          Validated.validNel(walletFile)
        } else {
          Validated.invalidNel(
            "Wallet file is mandatory"
          )
        }
      case None =>
        Validated.invalidNel("Wallet file is mandatory")
    }
  }

  def validateInputFile(
      someInputFile: Option[String],
      required: Boolean
  ): ValidatedNel[String, Option[String]] = {
    someInputFile match {
      case Some(inputFile) =>
        if (inputFile.trim().length >= 0) {
          // check if the file exists
          if (new java.io.File(inputFile).exists) {
            Validated.validNel(Some(inputFile))
          } else {
            Validated.invalidNel(
              "Input file does not exist"
            )
          }
        } else {
          Validated.invalidNel(
            "Input file must not be empty"
          )
        }
      case None =>
        if (required) Validated.invalidNel("Input file is required")
        else Validated.validNel(None)
    }
  }

  def validateNoPassphrase(somePassphrase: Option[String]) =
    somePassphrase match {
      case Some(_) =>
        Validated.invalidNel(
          "Passphrase must not be specified"
        )
      case None => Validated.validNel(None)
    }

  def validateNoPassword(password: String) =
    if (password.trim().length > 0) {
      Validated.invalidNel(
        "Password must not be specified"
      )
    } else {
      Validated.validNel(password)
    }

  def validatePassphrase(
      somePassphrase: Option[String]
  ): ValidatedNel[String, Option[String]] = {
    somePassphrase match {
      case Some(passphrase) =>
        if (passphrase.trim().length >= 0) {
          Validated.validNel(Some(passphrase))
        } else {
          Validated.invalidNel(
            "Passphrase must not be empty"
          )
        }
      case None => Validated.validNel(None)
    }
  }

}
