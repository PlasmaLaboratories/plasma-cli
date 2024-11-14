package org.plasmalabs.cli.impl

import cats.effect.kernel.Resource
import cats.effect.kernel.Sync
import org.plasmalabs.sdk.builders.TransactionBuilderApi
import org.plasmalabs.sdk.dataApi.WalletStateAlgebra
import org.plasmalabs.sdk.models._
import org.plasmalabs.sdk.models.box.Lock
import org.plasmalabs.sdk.utils.Encoding
import org.plasmalabs.sdk.wallet.WalletApi
import org.plasmalabs.indexer.services.Txo
import org.plasmalabs.quivr.models.KeyPair

import java.io.FileOutputStream

import TransactionBuilderApi.implicits._

trait GroupMintingOps[G[_]] extends CommonTxOps {

  import cats.implicits._

  implicit val sync: Sync[G]

  val tba: TransactionBuilderApi[G]

  val wsa: WalletStateAlgebra[G]

  val wa: WalletApi[G]

  def buildGroupTxAux(
    lvlTxos:                Seq[Txo],
    nonlvlTxos:             Seq[Txo],
    predicateFundsToUnlock: Lock.Predicate,
    amount:                 Long,
    fee:                    Long,
    someNextIndices:        Option[Indices],
    keyPair:                KeyPair,
    outputFile:             String,
    groupPolicy:            GroupPolicy,
    changeLock:             Option[Lock]
  ) = (if (lvlTxos.isEmpty) {
         Sync[G].raiseError(CreateTxError("No LVL txos found"))
       } else {
         changeLock match {
           case Some(lockPredicateForChange) =>
             tba
               .lockAddress(lockPredicateForChange)
               .flatMap { changeAddress =>
                 buildGroupTransaction(
                   lvlTxos ++ nonlvlTxos,
                   predicateFundsToUnlock,
                   lockPredicateForChange,
                   changeAddress,
                   amount,
                   fee,
                   someNextIndices,
                   keyPair,
                   outputFile,
                   groupPolicy
                 )
               }
           case None =>
             Sync[G].raiseError(
               CreateTxError("Unable to generate change lock")
             )
         }
       })

  private def buildGroupTransaction(
    txos:                   Seq[Txo],
    predicateFundsToUnlock: Lock.Predicate,
    lockForChange:          Lock,
    recipientLockAddress:   LockAddress,
    amount:                 Long,
    fee:                    Long,
    someNextIndices:        Option[Indices],
    keyPair:                KeyPair,
    outputFile:             String,
    groupPolicy:            GroupPolicy
  ): G[Unit] =
    for {
      changeAddress <- tba.lockAddress(
        lockForChange
      )
      eitherIoTransaction <- tba.buildGroupMintingTransaction(
        txos,
        predicateFundsToUnlock,
        groupPolicy,
        amount,
        recipientLockAddress,
        changeAddress,
        fee
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
