package org.plasmalabs.cli.controllers

import cats.Monad
import cats.effect.kernel.Sync
import cats.syntax.all.*
import org.plasmalabs.cli.params.models.*
import org.plasmalabs.sdk.codecs.AddressCodecs
import org.plasmalabs.sdk.dataApi.{IndexerQueryAlgebra, WalletStateAlgebra}
import org.plasmalabs.sdk.display.DisplayOps.DisplayTOps
import org.plasmalabs.sdk.display.txoDisplay

class IndexerQueryController[F[_]: Sync](
  walletStateAlgebra:  WalletStateAlgebra[F],
  indexerQueryAlgebra: IndexerQueryAlgebra[F]
) {

  def queryUtxoFromParams(
    someFromAddress:     Option[String],
    fromFellowship:      String,
    fromTemplate:        String,
    someFromInteraction: Option[Int],
    tokenType:           TokenType.Value = TokenType.all
  ): F[Either[String, String]] =
    someFromAddress
      .map(x => Sync[F].point(Some(x)))
      .getOrElse(
        walletStateAlgebra
          .getAddress(fromFellowship, fromTemplate, someFromInteraction)
      )
      .flatMap {
        case Some(address) =>
          indexerQueryAlgebra
            .queryUtxo(AddressCodecs.decodeAddress(address).toOption.get)
            .map(_.filter { x =>
              val value = x.transactionOutput.value.value
              if (tokenType == TokenType.lvl)
                value.isLvl
              else if (tokenType == TokenType.topl)
                value.isTopl
              else if (tokenType == TokenType.asset)
                value.isAsset
              else if (tokenType == TokenType.series)
                value.isSeries
              else if (tokenType == TokenType.group)
                value.isGroup
              else
                true
            })
            .map { txos =>
              if (txos.isEmpty) Left("No UTXO found")
              else
                Right(txos.map(_.display).mkString("\n\n"))
            }
            .attempt
            .map {
              case Left(_)     => Left("Problem contacting the network.")
              case Right(txos) => txos
            }
        case None => Monad[F].pure(Left("Address not found"))
      }

}
