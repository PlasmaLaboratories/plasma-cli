package org.plasmalabs.cli.params.mode

import org.plasmalabs.cli.{PlasmaCliMode, PlasmaCliParams, PlasmaCliSubCmd, TokenType}
import org.plasmalabs.sdk.utils.Encoding
import scopt.{OParser, OParserBuilder}

import java.io.File

trait SimpleMintingMode extends Args with Coordinates:

  val builder: OParserBuilder[PlasmaCliParams]

  import builder._

  def simpleMintingMode: OParser[Unit, PlasmaCliParams] =
    cmd("simple-minting")
      .action((_, c) => c.copy(mode = PlasmaCliMode.simpleminting))
      .text("Simple minting mode")
      .children(
        cmd("create")
          .action((_, c) => c.copy(subcmd = PlasmaCliSubCmd.create))
          .text("Create minting transaction")
          .children(
            ((coordinates ++ hostPortNetwork ++ keyfileAndPassword ++ Seq(
              walletDbArg,
              outputArg.required(),
              inputFileArg.required(),
              opt[String]("commitment")
                .action((x, c) => c.copy(someCommitment = Some(x)))
                .text(
                  "The commitment to use, 32 bytes in hexadecimal formal. (optional)"
                )
                .validate(x =>
                  Encoding
                    .decodeFromHex(x)
                    .fold(
                      _ => failure("Invalid commitment"),
                      a =>
                        if (a.length == 32) success
                        else failure("Invalid commitment: Length must be 32")
                    )
                ),
              opt[File]("ephemeralMetadata")
                .action((x, c) => c.copy(ephemeralMetadata = Some(x)))
                .text(
                  "A file containing the JSON metadata for the ephemeral metadata of the asset. (optional)"
                )
                .validate(x =>
                  if (x.exists()) success
                  else failure("Ephemeral metadata file does not exist")
                ),
              mintAmountArg,
              feeArg,
              mintTokenType.required(),
              checkConfig(c =>
                if (
                  c.mode == PlasmaCliMode.simpleminting &&
                  c.subcmd == PlasmaCliSubCmd.create &&
                  c.tokenType != TokenType.group &&
                  c.tokenType != TokenType.series &&
                  c.tokenType != TokenType.asset
                )
                  failure(
                    "Invalid asset to mint, supported assets are group, series and asset"
                  )
                else {
                  if (
                    c.mode == PlasmaCliMode.simpleminting &&
                    c.subcmd == PlasmaCliSubCmd.create
                  ) {
                    if (c.fromAddress.isDefined) {
                      failure(
                        "From address is not supported for minting"
                      )
                    } else if (c.tokenType == TokenType.asset) {
                      if (c.amount < 0) { // not set
                        success
                      } else {
                        failure(
                          "Amount already defined in the asset minting statement"
                        )
                      }
                    } else {
                      if (c.amount > 0) {
                        success
                      } else {
                        failure(
                          "Amount is mandatory for group and series minting"
                        )
                      }
                    }
                  } else
                    success
                }
              )
            ))): _*
          )
      )

end SimpleMintingMode
