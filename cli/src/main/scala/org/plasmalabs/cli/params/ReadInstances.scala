package org.plasmalabs.cli.params

import com.google.protobuf.ByteString
import org.plasmalabs.cli.params.models.*
import org.plasmalabs.sdk.codecs.AddressCodecs
import org.plasmalabs.sdk.models.{GroupId, LockAddress, SeriesId}
import org.plasmalabs.sdk.utils.Encoding

import scala.util.Try

/**
 *  scopt.Read instances
 */
trait ReadInstances:

  implicit val tokenTypeRead: scopt.Read[TokenType.Value] =
    Try(scopt.Read.reads(TokenType.withName)) match {
      case scala.util.Success(value) => value
      case scala.util.Failure(_) =>
        throw new IllegalArgumentException(
          "Invalid token type. Possible values: lvl, topl, asset, group, series, all"
        )
    }

  implicit val networkRead: scopt.Read[NetworkIdentifiers] =
    scopt.Read
      .reads(NetworkIdentifiers.fromString)
      .map {
        case Some(value) => value
        case None =>
          throw new IllegalArgumentException(
            "Invalid network. Possible values: mainnet, testnet, private"
          )
      }

  implicit val digestRead: scopt.Read[DigestType] =
    scopt.Read
      .reads(DigestType.withName)

  implicit val groupIdRead: scopt.Read[GroupId] =
    scopt.Read.reads { x =>
      val array = Encoding.decodeFromHex(x).toOption match {
        case Some(value) => value
        case None =>
          throw new IllegalArgumentException("Invalid group id")
      }
      GroupId(ByteString.copyFrom(array))
    }

  implicit val seriesIdRead: scopt.Read[SeriesId] =
    scopt.Read.reads { x =>
      val array = Encoding.decodeFromHex(x).toOption match {
        case Some(value) => value
        case None =>
          throw new IllegalArgumentException("Invalid series id")
      }
      SeriesId(ByteString.copyFrom(array))
    }

  implicit val lockAddressRead: scopt.Read[LockAddress] =
    scopt.Read.reads(
      AddressCodecs
        .decodeAddress(_)
        .toOption match {
        case None =>
          throw new IllegalArgumentException(
            "Invalid address, could not decode."
          )
        case Some(value) => value
      }
    )

end ReadInstances
