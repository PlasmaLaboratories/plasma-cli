package org.plasmalabs.cli.params.mode

import org.plasmalabs.cli.params.implicits.*
import org.plasmalabs.cli.{DigestType, NetworkIdentifiers, PlasmaCliParams, TokenType}
import org.plasmalabs.sdk.codecs.AddressCodecs
import org.plasmalabs.sdk.models.{GroupId, LockAddress, SeriesId}
import org.plasmalabs.sdk.utils.Encoding
import scopt.{OParser, OParserBuilder}

import java.io.File
import java.nio.file.Paths

trait Args:

  val builder: OParserBuilder[PlasmaCliParams]

  import builder._

  def inputFileArg: OParser[String, PlasmaCliParams] =
    opt[String]('i', "input")
      .action((x, c) => c.copy(someInputFile = Some(x)))
      .text("The input file. (mandatory)")
      .validate(x =>
        if (x.trim().isEmpty)
          failure("Input file may not be empty")
        else if (!new java.io.File(x).exists())
          failure(s"Input file $x does not exist")
        else
          success
      )

  def feeArg: OParser[Long, PlasmaCliParams] = opt[Long]("fee")
    .action((x, c) => c.copy(fee = x))
    .text("Fee paid for the transaction")
    .validate(x =>
      if (x > 0) success
      else failure("Amount must be greater than 0")
    )
    .required()

  def passphraseArg: OParser[String, PlasmaCliParams] =
    opt[String]('P', "passphrase")
      .action((x, c) => c.copy(somePassphrase = Some(x)))
      .text("Passphrase for the encrypted key. (optional))")
      .validate(x =>
        if (x.trim().isEmpty) failure("Passphrase may not be empty")
        else success
      )

  def amountArg: OParser[Long, PlasmaCliParams] = opt[Long]('a', "amount")
    .action((x, c) => c.copy(amount = x))
    .text("Amount to send")
    .validate(x =>
      if (x > 0) success
      else failure("Amount must be greater than 0")
    )

  def mintAmountArg: OParser[Long, PlasmaCliParams] = opt[Long]("mint-amount")
    .action((x, c) => c.copy(amount = x))
    .text("Amount to mint")
    .optional()

  def newwalletdbArg: OParser[String, PlasmaCliParams] = opt[String]("newwalletdb")
    .action((x, c) => c.copy(walletFile = x))
    .text("Wallet DB file. (mandatory)")
    .validate(x =>
      if (Paths.get(x).toFile.exists()) {
        failure("Wallet file " + x + " already exists")
      } else {
        success
      }
    )

  def outputArg: OParser[String, PlasmaCliParams] = opt[String]('o', "output")
    .action((x, c) => c.copy(someOutputFile = Some(x)))
    .text("The output file. (mandatory)")
    .validate(x =>
      if (x.trim().isEmpty) failure("Output file may not be empty")
      else if (Paths.get(x).toFile.exists()) {
        failure("Output file already exists")
      } else {
        success
      }
    )
    .required()

  def walletDbArg: OParser[String, PlasmaCliParams] = opt[String]("walletdb")
    .action((x, c) => c.copy(walletFile = x))
    .validate(validateWalletDbFile)
    .text("Wallet DB file. (mandatory)")
    .required()

  def templateNameArg: OParser[String, PlasmaCliParams] = opt[String]("template-name")
    .validate(x =>
      if (x.trim().isEmpty) failure("Template name may not be empty")
      else success
    )
    .action((x, c) => c.copy(templateName = x))
    .text("Name of the template. (mandatory)")
    .required()

  def networkArg: OParser[NetworkIdentifiers, PlasmaCliParams] = opt[NetworkIdentifiers]('n', "network")
    .action((x, c) => c.copy(network = x))
    .text(
      "Network name: Possible values: mainnet, testnet, private. (mandatory)"
    )

  def passwordArg: OParser[String, PlasmaCliParams] = opt[String]('w', "password")
    .action((x, c) => c.copy(password = x))
    .validate(x =>
      if (x.trim().isEmpty) failure("Password may not be empty")
      else success
    )
    .text("Password for the encrypted key. (mandatory)")
    .required()

  def fellowshipNameArg: OParser[String, PlasmaCliParams] = opt[String]("fellowship-name")
    .validate(x =>
      if (x.trim().isEmpty) failure("Fellowship name may not be empty")
      else success
    )
    .action((x, c) => c.copy(fellowshipName = x))
    .text("Name of the fellowship. (mandatory)")
    .required()

  def secretArg: OParser[String, PlasmaCliParams] = opt[String]("secret")
    .validate(x =>
      if (x.trim().isEmpty) failure("Secret may not be empty")
      else if (x.trim().getBytes().length > 32)
        failure("Secret (in bytes) may not be longer than 32 bytes")
      else success
    )
    .action((x, c) => c.copy(secret = x))
    .text("Secret to be encoded. (mandatory)")
    .required()

  def digestArg: OParser[DigestType, PlasmaCliParams] = opt[DigestType]("digest")
    .action((x, c) => c.copy(digest = x))
    .text("Digest algorithm used to encode the secret. (mandatory)")
    .required()

  def digestTextArg: OParser[String, PlasmaCliParams] = opt[String]("digest-text")
    .action((x, c) => c.copy(digestText = x))
    .validate { x =>
      if (x.trim().isEmpty) failure("Digest text may not be empty")
      else if (Encoding.decodeFromHex(x).isRight)
        success
      else failure("Invalid digest text")
    }
    .text("Digest text to query for preimage. (mandatory)")
    .required()

  def validateWalletDbFile(walletDbFile: String): Either[String, Unit] =
    if (walletDbFile.trim().isEmpty) failure("Wallet file may not be empty")
    else if (new java.io.File(walletDbFile).exists()) success
    else failure(s"Wallet file $walletDbFile does not exist")

  private def hostArg =
    opt[String]('h', "host")
      .action((x, c) => c.copy(host = x))
      .text("The host of the node. (mandatory)")
      .validate(x => if (x.trim().isEmpty) failure("Host may not be empty") else success)
      .required()

  private def portArg = opt[Int]("port")
    .action((x, c) => c.copy(nodePort = x))
    .text("Port Node node. (mandatory)")
    .validate(x =>
      if (x >= 0 && x <= 65536) success
      else failure("Port must be between 0 and 65536")
    )
    .required()

  def secureArg: OParser[Boolean, PlasmaCliParams] =
    opt[Boolean]('s', "secure")
      .action((x, c) => c.copy(secureConnection = x))
      .text("Enables the secure connection to the node. (optional)")

  def hostPort: Seq[OParser[? >: String & Int & Boolean, PlasmaCliParams]] = Seq(
    hostArg,
    portArg,
    secureArg
  )

  def groupId: OParser[Option[GroupId], PlasmaCliParams] = opt[Option[GroupId]]("group-id")
    .action((x, c) => c.copy(someGroupId = x))
    .text("Group id.")

  def seriesId: OParser[Option[SeriesId], PlasmaCliParams] = opt[Option[SeriesId]]("series-id")
    .action((x, c) => c.copy(someSeriesId = x))
    .text("Series id.")

  def hostPortNetwork: Seq[OParser[? >: NetworkIdentifiers & String & Int & Boolean, PlasmaCliParams]] =
    Seq(
      networkArg,
      hostArg,
      portArg,
      secureArg
    )

  def tokenType: OParser[TokenType.Value, PlasmaCliParams] = opt[TokenType.Value]("token")
    .action((x, c) => c.copy(tokenType = x))
    .text(
      "The token type. The valid token types are 'lvl', 'topl', 'asset', 'group', 'series', and 'all'"
    )

  def mintTokenType: OParser[TokenType.Value, PlasmaCliParams] = opt[TokenType.Value]("mint-token")
    .action((x, c) => c.copy(tokenType = x))
    .text(
      "The token type. The valid token types are 'asset', 'group', 'series'."
    )
    .validate(x =>
      if (x == TokenType.lvl || x == TokenType.topl || x == TokenType.all) {
        failure(
          "Invalid token type, supported types are asset, group and series"
        )
      } else {
        success
      }
    )

  def transferTokenType: OParser[TokenType.Value, PlasmaCliParams] = opt[TokenType.Value]("transfer-token")
    .action((x, c) => c.copy(tokenType = x))
    .text(
      "The token type. The valid token types are 'lvl', 'asset', 'group', 'series'."
    )
    .validate(x =>
      if (x == TokenType.topl || x == TokenType.all) {
        failure(
          "Invalid token type, supported types are lvl, asset, group and series"
        )
      } else {
        success
      }
    )
    .required()

  def fromAddress: OParser[Option[String], PlasmaCliParams] = opt[Option[String]]("from-address")
    .action((x, c) => c.copy(fromAddress = x))
    .text("Address where we are sending the funds from")
    .validate(someAddress =>
      someAddress
        .map(AddressCodecs.decodeAddress)
        .map {
          case Left(_)  => failure("Invalid from address")
          case Right(_) => success
        }
        .getOrElse(success)
    )

  private def keyfileArg = opt[String]('k', "keyfile")
    .action((x, c) => c.copy(someKeyFile = Some(x)))
    .text("The key file.")
    .validate(x =>
      if (x.trim().isEmpty) failure("Key file may not be empty")
      else if (!new java.io.File(x).exists())
        failure(s"Key file $x does not exist")
      else success
    )
    .required()

  def keyfileAndPassword: Seq[OParser[String, PlasmaCliParams]] =
    Seq(
      keyfileArg,
      passwordArg
    )

end Args
