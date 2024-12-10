package org.plasmalabs.cli.params.mode

import org.plasmalabs.cli.params.CliParamsParser.{hostPortNetwork, keyfileAndPassword, walletDbArg}
import org.plasmalabs.cli.params.ReadInstances
import org.plasmalabs.cli.params.models.*
import org.plasmalabs.sdk.constants.NetworkConstants
import org.plasmalabs.sdk.models.{GroupId, LockAddress, SeriesId}
import scopt.OParser

trait SimpleTransactionMode extends Coordinates with Args with ReadInstances:

  private def checkAddress(
    lockAddress: LockAddress,
    networkId:   NetworkIdentifiers
  ) =
    if (lockAddress.ledger != NetworkConstants.MAIN_LEDGER_ID) {
      builder.failure("Invalid ledger id")
    } else if (lockAddress.network != networkId.networkId) {
      builder.failure(
        "Invalid network id. Address is using a different network id than the one passed as a parameter: " + networkId
          .toString()
      )
    } else {
      builder.success
    }

  private def checkTokenAndId(
    tokenType: TokenType.Value,
    groupId:   Option[GroupId],
    seriesId:  Option[SeriesId]
  ): Either[String, Unit] =
    (tokenType, groupId, seriesId) match {
      case (TokenType.group, Some(_), None) =>
        builder.success
      case (TokenType.series, None, Some(_)) =>
        builder.success
      case (TokenType.asset, Some(_), Some(_)) =>
        builder.success
      case (TokenType.lvl, None, None) =>
        builder.success
      case _ =>
        builder.failure(
          "Exactly group and groupId together, or series and seriesId, or only lvl must be specified"
        )
    }

  def simpleTransactionMode =
    builder
      .cmd("simple-transaction")
      .action((_, c) => c.copy(mode = CliMode.simpletransaction))
      .text("Simple transaction mode")
      .children(
        builder
          .cmd("create")
          .action((_, c) => c.copy(subcmd = CliSubCmd.create))
          .text("Create transaction")
          .children(
            ((coordinates ++ changeCoordinates ++ hostPortNetwork ++ keyfileAndPassword ++ Seq(
              walletDbArg,
              outputArg.required()
            )) ++
            Seq(
              feeArg,
              builder
                .opt[Option[LockAddress]]('t', "to")
                .action((x, c) => c.copy(toAddress = x))
                .text(
                  "Address to send LVLs to. (mandatory if to-fellowship and to-template are not provided)"
                ),
              builder
                .opt[Option[String]]("to-fellowship")
                .action((x, c) => c.copy(someToFellowship = x))
                .text(
                  "Fellowship to send LVLs to. (mandatory if to is not provided)"
                ),
              builder
                .opt[Option[String]]("to-template")
                .action((x, c) => c.copy(someToTemplate = x))
                .text(
                  "Template to send LVLs to. (mandatory if to is not provided)"
                ),
              amountArg,
              transferTokenType,
              groupId,
              seriesId,
              builder.checkConfig { c =>
                if (c.mode == CliMode.simpletransaction && c.subcmd == CliSubCmd.create)
                  if (c.fromAddress.isDefined) {
                    builder.failure(
                      "From address is not supported for simple transactions"
                    )
                  } else
                    (c.toAddress, c.someToFellowship, c.someToTemplate) match {
                      case (Some(address), None, None) =>
                        checkAddress(address, c.network).flatMap(_ =>
                          checkTokenAndId(
                            c.tokenType,
                            c.someGroupId,
                            c.someSeriesId
                          )
                        )
                      case (None, Some(_), Some(_)) =>
                        checkTokenAndId(
                          c.tokenType,
                          c.someGroupId,
                          c.someSeriesId
                        )
                      case _ =>
                        builder.failure(
                          "Exactly toFellowship and toTemplate together or only toAddress must be specified"
                        )
                    }
                else
                  builder.success
              }
            )): _*
          )
      )

end SimpleTransactionMode
