package org.plasmalabs.cli.impl

import cats.effect.kernel.{Resource, Sync}
import com.google.protobuf.ByteString
import com.google.protobuf.struct.Struct
import io.circe.Json
import org.plasmalabs.indexer.services.Txo
import org.plasmalabs.quivr.models.KeyPair
import org.plasmalabs.sdk.builders.TransactionBuilderApi
import org.plasmalabs.sdk.dataApi.WalletStateAlgebra
import org.plasmalabs.sdk.models.box.Lock
import org.plasmalabs.sdk.models.{AssetMintingStatement, Indices, LockAddress}
import org.plasmalabs.sdk.utils.Encoding
import org.plasmalabs.sdk.wallet.WalletApi

import java.io.FileOutputStream

import TransactionBuilderApi.implicits.*

trait AssetMintingOps[G[_]] extends CommonTxOps {

  import cats.implicits._

  implicit val sync: Sync[G]

  val tba: TransactionBuilderApi[G]

  val wsa: WalletStateAlgebra[G]

  val wa: WalletApi[G]

  def buildAssetTxAux(
    keyPair:               KeyPair,
    outputFile:            String,
    lvlTxos:               Seq[Txo],
    nonLvlTxos:            Seq[Txo],
    groupTxo:              Txo,
    seriesTxo:             Txo,
    lockAddrToUnlock:      LockAddress,
    lockPredicateFrom:     Lock.Predicate,
    fee:                   Long,
    someNextIndices:       Option[Indices],
    assetMintingStatement: AssetMintingStatement,
    ephemeralMetadata:     Option[Json],
    commitment:            Option[ByteString],
    changeLock:            Option[Lock]
  ) = (if (lvlTxos.isEmpty) {
         Sync[G].raiseError(CreateTxError("No LVL txos found"))
       } else {
         changeLock match {
           case Some(lockPredicateForChange) =>
             tba
               .lockAddress(lockPredicateForChange)
               .flatMap { changeAddress =>
                 buildAssetTransaction(
                   keyPair,
                   outputFile,
                   lvlTxos ++ nonLvlTxos :+ groupTxo :+ seriesTxo,
                   Map(lockAddrToUnlock -> lockPredicateFrom),
                   lockPredicateForChange,
                   changeAddress,
                   fee,
                   assetMintingStatement,
                   ephemeralMetadata.map(toStruct(_).getStructValue),
                   commitment,
                   someNextIndices
                 )
               }
           case None =>
             Sync[G].raiseError(
               CreateTxError("Unable to generate change lock")
             )
         }
       })

  private def buildAssetTransaction(
    keyPair:               KeyPair,
    outputFile:            String,
    txos:                  Seq[Txo],
    lockPredicateFrom:     Map[LockAddress, Lock.Predicate],
    lockForChange:         Lock,
    recipientLockAddress:  LockAddress,
    fee:                   Long,
    assetMintingStatement: AssetMintingStatement,
    ephemeralMetadata:     Option[Struct],
    commitment:            Option[ByteString],
    someNextIndices:       Option[Indices]
  ): G[Unit] =
    for {
      changeAddress <- tba.lockAddress(
        lockForChange
      )
      eitherIoTransaction <- tba.buildAssetMintingTransaction(
        assetMintingStatement,
        txos,
        lockPredicateFrom,
        fee,
        recipientLockAddress,
        changeAddress,
        ephemeralMetadata,
        commitment
      )
      ioTransaction <- Sync[G].fromEither(eitherIoTransaction)
      // Only save to wallet interaction if there is a change output in the transaction
      _ <-
        if (ioTransaction.outputs.length >= 2) for {
          vk <- someNextIndices
            .map(nextIndices =>
              wa
                .deriveChildKeys(keyPair, nextIndices)
                .map(_.vk)
            )
            .sequence
          _ <- wsa.updateWalletState(
            Encoding.encodeToBase58Check(
              lockForChange.getPredicate.toByteArray
            ),
            changeAddress.toBase58(),
            vk.map(_ => "ExtendedEd25519"),
            vk.map(x => Encoding.encodeToBase58(x.toByteArray)),
            someNextIndices.get
          )
        } yield ()
        else {
          Sync[G].delay(())
        }
      _ <-
        Resource
          .make(
            Sync[G]
              .delay(
                new FileOutputStream(outputFile)
              )
          )(fos => Sync[G].delay(fos.close()))
          .use { fos =>
            Sync[G]
              .delay(ioTransaction.writeTo(fos))
              .onError(_ =>
                Sync[G].raiseError(
                  CannotSerializeProtobufFile(
                    "Cannot write to file"
                  )
                )
              )
          }
    } yield ()
}
