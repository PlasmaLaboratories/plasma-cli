package org.plasmalabs.cli.parsers

import cats.effect.kernel.{Resource, Sync}
import com.google.protobuf.struct.Value
import io.circe.Json
import org.plasmalabs.cli.impl.*
import org.plasmalabs.sdk.models.*
import org.plasmalabs.sdk.models.box.{FungibilityType, QuantityDescriptorType}

import scala.io.BufferedSource

trait SeriesPolicyParser[F[_]] {

  def parseSeriesPolicy(
    inputFileRes: Resource[F, BufferedSource]
  ): F[Either[CommonParserError, SeriesPolicy]]
}

object SeriesPolicyParser {

  private case class SeriesPolicyInternal(
    label:                   String,
    tokenSupply:             Option[Int],
    registrationUtxo:        String,
    fungibility:             String,
    quantityDescriptor:      String,
    ephemeralMetadataScheme: Option[Json],
    permanentMetadataScheme: Option[Json]
  )

  def make[F[_]: Sync](
    networkId: Int
  ): SeriesPolicyParser[F] & CommonTxOps = new SeriesPolicyParser[F] with CommonTxOps {

    import cats.implicits.*
    import io.circe.generic.auto.*
    import io.circe.yaml

    private def seriesPolicyToPBSeriesPolicy(
      seriesPolicy: SeriesPolicyInternal
    ): F[SeriesPolicy] =
      for {
        label <-
          Sync[F].delay(
            seriesPolicy.label
          )
        someTokenSupply <- Sync[F].delay(seriesPolicy.tokenSupply)
        registrationUtxo <- Sync[F].fromEither(
          CommonParsingOps.parseTransactionOuputAddress(
            networkId,
            seriesPolicy.registrationUtxo
          )
        )
        fungibility <- Sync[F].delay(
          seriesPolicy.fungibility match {
            case "group"            => FungibilityType.GROUP
            case "series"           => FungibilityType.SERIES
            case "group-and-series" => FungibilityType.GROUP_AND_SERIES
            case _ =>
              throw InvalidFungibility(
                "Invalid fungibility: " + seriesPolicy.fungibility
              )
          }
        )
        quantityDescriptor <- Sync[F].delay(
          seriesPolicy.quantityDescriptor match {
            case "liquid"       => QuantityDescriptorType.LIQUID
            case "accumulator"  => QuantityDescriptorType.ACCUMULATOR
            case "fractionable" => QuantityDescriptorType.FRACTIONABLE
            case "immutable"    => QuantityDescriptorType.IMMUTABLE
            case _ =>
              throw InvalidQuantityDescriptor(
                "Invalid quantity descriptor: " + seriesPolicy.quantityDescriptor
              )
          }
        )
        ephemeralMetadataScheme <- Sync[F].delay(
          seriesPolicy.ephemeralMetadataScheme.map(toStruct(_).kind match {
            case Value.Kind.StructValue(struct) => struct
            case _ =>
              throw InvalidMetadataScheme(
                "Invalid ephemeral metadata scheme: " + seriesPolicy.ephemeralMetadataScheme
              )
          })
        )
        permanentMetadataScheme <- Sync[F].delay(
          seriesPolicy.permanentMetadataScheme.map(toStruct(_).kind match {
            case Value.Kind.StructValue(struct) => struct
            case _ =>
              throw InvalidMetadataScheme(
                "Invalid permanent metadata scheme: " + seriesPolicy.ephemeralMetadataScheme
              )
          })
        )
      } yield SeriesPolicy(
        label,
        someTokenSupply,
        registrationUtxo,
        quantityDescriptor,
        fungibility,
        ephemeralMetadataScheme,
        permanentMetadataScheme
      )

    override def parseSeriesPolicy(
      inputFileRes: Resource[F, BufferedSource]
    ): F[Either[CommonParserError, SeriesPolicy]] = (for {
      inputString <- inputFileRes.use(file => Sync[F].blocking(file.getLines().mkString("\n")))
      seriesPolicy <-
        Sync[F].fromEither(
          yaml.v12.parser
            .parse(inputString)
            .flatMap(tx => tx.as[SeriesPolicyInternal])
            .leftMap { e =>
              InvalidYaml(e)
            }
        )
      sp <- seriesPolicyToPBSeriesPolicy(seriesPolicy)
    } yield sp).attempt.map {
      case Right(value)               => Right(value)
      case Left(e: CommonParserError) => Left(e)
      case Left(e)                    => Left(InvalidYaml(e))
    }

  }

}
