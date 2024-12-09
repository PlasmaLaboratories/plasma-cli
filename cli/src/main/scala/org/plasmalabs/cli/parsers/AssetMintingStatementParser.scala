package org.plasmalabs.cli.parsers

import cats.effect.kernel.{Resource, Sync}
import com.google.protobuf.struct.Value
import io.circe.Json
import org.plasmalabs.cli.impl.{CommonParsingOps, CommonTxOps}
import org.plasmalabs.sdk.models.AssetMintingStatement as PBAssetMintingStatement

import scala.io.BufferedSource

trait AssetMintingStatementParser[F[_]] {

  def parseAssetMintingStatement(
    inputFileRes: Resource[F, BufferedSource]
  ): F[Either[CommonParserError, PBAssetMintingStatement]]
}

object AssetMintingStatementParser {

  private case class AssetMintingStatement(
    groupTokenUtxo:    String,
    seriesTokenUtxo:   String,
    quantity:          Long,
    permanentMetadata: Option[Json]
  )

  def make[F[_]: Sync](networkId: Int): AssetMintingStatementParser[F] & CommonTxOps =
    new AssetMintingStatementParser[F] with CommonTxOps {

      import cats.implicits.*
      import io.circe.generic.auto.*
      import io.circe.yaml
      import org.plasmalabs.sdk.syntax.*

      private def assetMintingStatementToPBAMS(
        assetMintingStatement: AssetMintingStatement
      ): F[PBAssetMintingStatement] = for {
        groupTokenUtxo <- Sync[F].fromEither(
          CommonParsingOps.parseTransactionOuputAddress(
            networkId,
            assetMintingStatement.groupTokenUtxo
          )
        )
        seriesTokenUtxo <- Sync[F].fromEither(
          CommonParsingOps.parseTransactionOuputAddress(
            networkId,
            assetMintingStatement.seriesTokenUtxo
          )
        )
        permanentMetadata <- Sync[F].delay(
          assetMintingStatement.permanentMetadata.map(
            toStruct(_).kind match {
              case Value.Kind.StructValue(struct) => struct
              case _ =>
                throw InvalidMetadataScheme(
                  "Invalid permanent metadata: " + assetMintingStatement.permanentMetadata
                )
            }
          )
        )
      } yield PBAssetMintingStatement(
        groupTokenUtxo = groupTokenUtxo,
        seriesTokenUtxo = seriesTokenUtxo,
        quantity = assetMintingStatement.quantity,
        permanentMetadata = permanentMetadata
      )

      override def parseAssetMintingStatement(
        inputFileRes: Resource[F, BufferedSource]
      ): F[Either[CommonParserError, PBAssetMintingStatement]] = (for {
        inputString <- inputFileRes.use(file => Sync[F].blocking(file.getLines().mkString("\n")))
        assetMintingStatement <-
          Sync[F].fromEither(
            yaml.v12.parser
              .parse(inputString)
              .flatMap(tx => tx.as[AssetMintingStatement])
              .leftMap { e =>
                InvalidYaml(e)
              }
          )
        ams <- assetMintingStatementToPBAMS(assetMintingStatement)
      } yield ams).attempt.map {
        case Right(value)               => Right(value)
        case Left(e: CommonParserError) => Left(e)
        case Left(e)                    => Left(InvalidYaml(e))
      }

    }

}
