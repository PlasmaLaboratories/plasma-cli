package xyz.stratalab.strata.cli.impl

import xyz.stratalab.sdk.codecs.AddressCodecs
import xyz.stratalab.sdk.constants.NetworkConstants
import xyz.stratalab.sdk.models.TransactionId
import xyz.stratalab.sdk.models.TransactionOutputAddress
import xyz.stratalab.sdk.models.box.Value
import xyz.stratalab.sdk.models.transaction.UnspentTransactionOutput
import xyz.stratalab.sdk.utils.Encoding
import com.google.protobuf.ByteString
import quivr.models.Int128

import scala.util.Try

object CommonParsingOps {

  import cats.implicits._

  def parseUnspentTransactionOutput(
      lockAddressString: String,
      value: Long
  ): Either[CommonParserError, UnspentTransactionOutput] =
    for {
      lockAddress <-
        AddressCodecs
          .decodeAddress(lockAddressString)
          .leftMap(_ =>
            InvalidAddress(
              "Invalid address for unspent transaction output: " + lockAddressString
            ): CommonParserError
          )
    } yield UnspentTransactionOutput(
      lockAddress,
      Value(
        Value.Value.Lvl(
          Value.LVL(
            Int128(ByteString.copyFrom(BigInt(value).toByteArray))
          )
        )
      )
    )

  def parseTransactionOuputAddress(
      networkId: Int,
      address: String
  ) = for {
    sp <- Right(address.split("#"))
    idx <- Try(sp(1).toInt).toEither.leftMap(_ =>
      InvalidAddress("Invalid index for address: " + address)
    )
    txIdByteArray <- Encoding
      .decodeFromBase58(sp(0))
      .leftMap(_ => InvalidAddress("Invalid transaction id for: " + sp(0)))
    txId <- Right(
      TransactionId(
        ByteString.copyFrom(
          txIdByteArray
        )
      )
    )
  } yield TransactionOutputAddress(
    networkId,
    NetworkConstants.MAIN_LEDGER_ID,
    idx,
    txId
  )
}
