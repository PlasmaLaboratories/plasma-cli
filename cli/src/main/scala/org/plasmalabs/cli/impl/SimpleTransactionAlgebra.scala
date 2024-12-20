package org.plasmalabs.cli.impl

import cats.effect.kernel.{Resource, Sync}
import org.plasmalabs.indexer.services.Txo
import org.plasmalabs.quivr.models.KeyPair
import org.plasmalabs.sdk.builders.TransactionBuilderApi
import org.plasmalabs.sdk.codecs.AddressCodecs
import org.plasmalabs.sdk.dataApi.{IndexerQueryAlgebra, WalletStateAlgebra}
import org.plasmalabs.sdk.models.box.Lock
import org.plasmalabs.sdk.models.{Indices, LockAddress}
import org.plasmalabs.sdk.syntax.ValueTypeIdentifier
import org.plasmalabs.sdk.utils.Encoding
import org.plasmalabs.sdk.wallet.WalletApi

import java.io.FileOutputStream

trait SimpleTransactionAlgebra[F[_]] {

  def createSimpleTransactionFromParams(
    keyfile:               String,
    password:              String,
    fromFellowship:        String,
    fromTemplate:          String,
    someFromInteraction:   Option[Int],
    someChangeFellowship:  Option[String],
    someChangeTemplate:    Option[String],
    someChangeInteraction: Option[Int],
    someToAddress:         Option[LockAddress],
    someToFellowship:      Option[String],
    someToTemplate:        Option[String],
    amount:                Long,
    fee:                   Long,
    outputFile:            String,
    tokenType:             ValueTypeIdentifier
  ): F[Either[SimpleTransactionAlgebraError, Unit]]

}

object SimpleTransactionAlgebra {

  def make[F[_]: Sync](
    walletApi:             WalletApi[F],
    walletStateApi:        WalletStateAlgebra[F],
    utxoAlgebra:           IndexerQueryAlgebra[F],
    transactionBuilderApi: TransactionBuilderApi[F],
    walletManagementUtils: WalletManagementUtils[F]
  ) =
    new SimpleTransactionAlgebra[F] {

      private def buildTransaction(
        txos:                   Seq[Txo],
        someChangeFellowship:   Option[String],
        someChangeTemplate:     Option[String],
        someChangeInteraction:  Option[Int],
        predicateFundsToUnlock: Lock.Predicate,
        lockForChange:          Lock,
        recipientLockAddress:   LockAddress,
        amount:                 Long,
        fee:                    Long,
        someNextIndices:        Option[Indices],
        keyPair:                KeyPair,
        outputFile:             String,
        typeIdentifier:         ValueTypeIdentifier
      ) = {
        import cats.implicits._
        import TransactionBuilderApi.implicits._
        for {
          lockChange <- transactionBuilderApi.lockAddress(lockForChange)
          eitherIoTransaction <- transactionBuilderApi
            .buildTransferAmountTransaction(
              typeIdentifier,
              txos,
              predicateFundsToUnlock,
              amount,
              recipientLockAddress,
              lockChange,
              fee
            )
          ioTransaction <- Sync[F].fromEither(eitherIoTransaction)
          // Only save to wallet interaction if there is a change output in the transaction
          nextIndicesExist <- (
            someChangeFellowship,
            someChangeTemplate,
            someChangeInteraction
          ) match {
            case (
                  Some(changeFellowship),
                  Some(changeTemplate),
                  Some(changeState)
                ) =>
              walletStateApi
                .getCurrentIndicesForFunds(
                  changeFellowship,
                  changeTemplate,
                  Some(changeState)
                )
                .map(_.isDefined)
            case _ => Sync[F].point(false)
          }
          _ <-
            if (ioTransaction.outputs.length >= 2 && !nextIndicesExist) for {
              lockAddress <-
                transactionBuilderApi.lockAddress(
                  lockForChange
                )
              vk <- someNextIndices
                .map(nextIndices =>
                  walletApi
                    .deriveChildKeys(keyPair, nextIndices)
                    .map(_.vk)
                )
                .sequence

              _ <- walletStateApi.updateWalletState(
                Encoding.encodeToBase58Check(
                  lockForChange.getPredicate.toByteArray
                ),
                lockAddress.toBase58(),
                vk.map(_ => "ExtendedEd25519"),
                vk.map(x => Encoding.encodeToBase58(x.toByteArray)),
                someNextIndices.get
              )
            } yield ()
            else {
              Sync[F].delay(())
            }
          _ <- Resource
            .make(
              Sync[F]
                .delay(
                  new FileOutputStream(outputFile)
                )
            )(fos => Sync[F].delay(fos.close()))
            .use { fos =>
              Sync[F]
                .delay(ioTransaction.writeTo(fos))
                .adaptErr(_ =>
                  CannotSerializeProtobufFile(
                    "Cannot write to file"
                  ): SimpleTransactionAlgebraError
                )
            }
        } yield ()
      }

      override def createSimpleTransactionFromParams(
        keyfile:               String,
        password:              String,
        fromFellowship:        String,
        fromTemplate:          String,
        someFromInteraction:   Option[Int],
        someChangeFellowship:  Option[String],
        someChangeTemplate:    Option[String],
        someChangeInteraction: Option[Int],
        someToAddress:         Option[LockAddress],
        someToFellowship:      Option[String],
        someToTemplate:        Option[String],
        amount:                Long,
        fee:                   Long,
        outputFile:            String,
        tokenType:             ValueTypeIdentifier
      ): F[Either[SimpleTransactionAlgebraError, Unit]] = {
        import cats.implicits._

        (for {
          keyPair <- walletManagementUtils
            .loadKeys(
              keyfile,
              password
            )
            .adaptErr(_ =>
              CreateTxError(
                "Cannot load keyfile: Check password and keyfile"
              ): SimpleTransactionAlgebraError
            )
          someCurrentIndices <- walletStateApi.getCurrentIndicesForFunds(
            fromFellowship,
            fromTemplate,
            someFromInteraction
          )
          predicateFundsToUnlock <- someCurrentIndices
            .map(currentIndices => walletStateApi.getLockByIndex(currentIndices))
            .sequence
            .map(_.flatten.map(Lock().withPredicate(_)))
          someNextIndices <-
            (
              someChangeFellowship,
              someChangeTemplate,
              someChangeInteraction
            ) match {
              case (Some(fellowship), Some(template), Some(interaction)) =>
                walletStateApi.getCurrentIndicesForFunds(
                  fellowship,
                  template,
                  Some(interaction)
                )
              case _ =>
                walletStateApi.getNextIndicesForFunds(
                  fromFellowship,
                  fromTemplate
                )
            }
          // Generate a new lock for the change, if possible
          changeLock <- someNextIndices
            .map { idx =>
              walletStateApi
                .getLock(fromFellowship, fromTemplate, idx.z)
            }
            .sequence
            .map(_.flatten)
          fromAddress <- transactionBuilderApi.lockAddress(
            predicateFundsToUnlock.get
          )
          response <- utxoAlgebra
            .queryUtxo(fromAddress)
            .attempt
            .flatMap {
              _ match {
                case Left(_) =>
                  Sync[F].raiseError(
                    CreateTxError("Problem contacting network")
                  ): F[Seq[Txo]]
                case Right(txos) => Sync[F].pure(txos: Seq[Txo])
              }
            }
          txos = response
            .filter(x =>
              !x.transactionOutput.value.value.isTopl &&
              !x.transactionOutput.value.value.isUpdateProposal
            )
          // either toAddress or both toTemplate and toFellowship must be defined
          toAddressOpt <- (
            someToAddress,
            someToFellowship,
            someToTemplate
          ) match {
            case (Some(address), _, _) => Sync[F].point(Some(address))
            case (None, Some(fellowship), Some(template)) =>
              walletStateApi
                .getAddress(fellowship, template, None)
                .map(
                  _.flatMap(addrStr => AddressCodecs.decodeAddress(addrStr).toOption)
                )
            case _ => Sync[F].point(None)
          }
          _ <-
            (if (txos.isEmpty) {
               Sync[F].raiseError(CreateTxError("No LVL txos found"))
             } else {
               (changeLock, toAddressOpt) match {
                 case (Some(lockPredicateForChange), Some(toAddress)) =>
                   buildTransaction(
                     txos,
                     someChangeFellowship,
                     someChangeTemplate,
                     someChangeInteraction,
                     predicateFundsToUnlock.get.getPredicate,
                     lockPredicateForChange,
                     toAddress,
                     amount,
                     fee,
                     someNextIndices,
                     keyPair,
                     outputFile,
                     tokenType
                   )
                 case (None, _) =>
                   Sync[F].raiseError(
                     CreateTxError("Unable to generate change lock")
                   )
                 case (_, _) =>
                   Sync[F].raiseError(
                     CreateTxError("Unable to derive recipient address")
                   )
               }
             })
        } yield ()).attempt.map(e =>
          e match {
            case Right(_)                               => ().asRight
            case Left(e: SimpleTransactionAlgebraError) => e.asLeft
            case Left(e) =>
              e.printStackTrace()
              UnexpectedError(e.getMessage()).asLeft
          }
        )

      }
    }
}
