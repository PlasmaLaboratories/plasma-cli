package org.plasmalabs.cli.impl

import cats.effect.kernel.{Resource, Sync}
import io.grpc.ManagedChannel
import org.plasmalabs.crypto.signing.ExtendedEd25519
import org.plasmalabs.quivr.models.KeyPair
import org.plasmalabs.quivr.runtime.{QuivrRuntimeError, QuivrRuntimeErrors}
import org.plasmalabs.sdk.Context
import org.plasmalabs.sdk.dataApi.{NodeQueryAlgebra, WalletStateAlgebra}
import org.plasmalabs.sdk.models.transaction.IoTransaction
import org.plasmalabs.sdk.models.{Datum, Event}
import org.plasmalabs.sdk.syntax.cryptoToPbKeyPair
import org.plasmalabs.sdk.utils.Encoding
import org.plasmalabs.sdk.validation.TransactionSyntaxError.{EmptyInputs, InvalidDataLength}
import org.plasmalabs.sdk.validation.{
  TransactionAuthorizationError,
  TransactionSyntaxError,
  TransactionSyntaxInterpreter
}
import org.plasmalabs.sdk.wallet.{CredentiallerInterpreter, WalletApi}

import java.io.{FileInputStream, FileOutputStream}

trait TransactionAlgebra[F[_]] {

  def proveTransactionFromParams(
    inputRes:  Resource[F, FileInputStream],
    keyFile:   String,
    password:  String,
    outputRes: Resource[F, FileOutputStream]
  ): F[Either[SimpleTransactionAlgebraError, Unit]]

  def broadcastTransactionFromParams(
    provedTxFile: String
  ): F[Either[SimpleTransactionAlgebraError, String]]

}

object TransactionAlgebra {

  def make[F[_]: Sync](
    walletApi:             WalletApi[F],
    walletStateApi:        WalletStateAlgebra[F],
    walletManagementUtils: WalletManagementUtils[F],
    channelResource:       Resource[F, ManagedChannel]
  ): TransactionAlgebra[F] =
    new TransactionAlgebra[F] {

      private def quivrErrorToString(qre: QuivrRuntimeError): String =
        qre match {
          case QuivrRuntimeErrors.ValidationError
                .EvaluationAuthorizationFailed(_, _) =>
            "Evaluation authorization failed"
          case QuivrRuntimeErrors.ValidationError.MessageAuthorizationFailed(
                _
              ) =>
            "Message authorization failed"
          case QuivrRuntimeErrors.ValidationError.LockedPropositionIsUnsatisfiable =>
            "Locked proposition is unsatisfiable"
          case QuivrRuntimeErrors.ValidationError.UserProvidedInterfaceFailure =>
            "User provided interface failure"
          case _: QuivrRuntimeError => "Unknown error: " + qre.toString
        }

      override def broadcastTransactionFromParams(
        provedTxFile: String
      ): F[Either[SimpleTransactionAlgebraError, String]] = {
        import org.plasmalabs.sdk.models.transaction.IoTransaction
        import cats.implicits._
        val inputRes = Resource
          .make {
            Sync[F]
              .delay(new FileInputStream(provedTxFile))
          }(fos => Sync[F].delay(fos.close()))

        (for {
          provedTransaction <-
            inputRes.use(fis =>
              Sync[F]
                .blocking(IoTransaction.parseFrom(fis))
                .adaptErr { case _ =>
                  InvalidProtobufFile("Invalid protobuf file")
                }
            )
          _           <- validateTx(provedTransaction)
          validations <- checkSignatures(provedTransaction)
          _ <- Sync[F]
            .raiseError(
              new IllegalStateException(
                "Error validating transaction: " + validations
                  .map {
                    case TransactionAuthorizationError
                          .AuthorizationFailed(errors) =>
                      errors.map(quivrErrorToString).mkString(", ")
                    case _ =>
                      "Contextual or permanent error was found."
                  }
                  .mkString(", ")
              )
            )
            .whenA(validations.nonEmpty)
          response <- NodeQueryAlgebra
            .make[F](channelResource)
            .broadcastTransaction(provedTransaction)
            .map(_ => provedTransaction)
            .adaptErr { e =>
              e.printStackTrace()
              NetworkProblem("Problem connecting to node")
            }
        } yield response).attempt.map {
          case Right(tx) =>
            import org.plasmalabs.sdk.syntax._
            Encoding.encodeToBase58(tx.id.value.toByteArray).asRight
          case Left(e: SimpleTransactionAlgebraError) => e.asLeft
          case Left(e)                                => UnexpectedError(e.getMessage).asLeft
        }
      }

      private def checkSignatures(tx: IoTransaction) = {
        import cats.implicits._
        val mockKeyPair: KeyPair = (new ExtendedEd25519).deriveKeyPairFromSeed(
          Array.fill(96)(0: Byte)
        )
        for {
          credentialer <- Sync[F].delay(
            CredentiallerInterpreter
              .make[F](
                walletApi,
                walletStateApi,
                mockKeyPair
              )
          )
          tipBlockHeader <- NodeQueryAlgebra
            .make[F](channelResource)
            .blockByDepth(1L)
            .map(_.get._2)
            .adaptErr { e =>
              e.printStackTrace()
              NetworkProblem("Problem connecting to node to get context")
            }
          context <- Sync[F].delay(
            Context[F](
              tx,
              tipBlockHeader.slot,
              Map(
                "header" -> Datum().withHeader(
                  Datum.Header(Event.Header(tipBlockHeader.height))
                )
              ).lift
            )
          )
          validationErrors <- credentialer.validate(tx, context)
        } yield validationErrors
      }

      private def validateTx(tx: IoTransaction) = {
        import cats.implicits._
        for {
          syntaxValidator <- Sync[F]
            .delay(
              TransactionSyntaxInterpreter
                .make[F]()
            )
          valResult <- syntaxValidator.validate(tx)
          _ <- valResult match {
            case Left(errors) =>
              Sync[F].raiseError(
                ValidateTxErrpr(
                  "Error validating transaction: " + errors
                    .map {
                      case InvalidDataLength =>
                        "Invalid data length. Transaction too big."
                      case EmptyInputs =>
                        "No inputs in transaction."
                      case TransactionSyntaxError.DuplicateInput(_) =>
                        "There are duplicate inputs in the transactions."
                      case TransactionSyntaxError.ExcessiveOutputsCount =>
                        "Too many outputs in the transaction."
                      case TransactionSyntaxError.InvalidTimestamp(_) =>
                        "The timestamp for the transaction is invalid."
                      case TransactionSyntaxError.InvalidSchedule(_) =>
                        "The schedule for the transaction is invalid."
                      case TransactionSyntaxError.NonPositiveOutputValue(_) =>
                        "One of the output values is not positive."
                      case TransactionSyntaxError
                            .InsufficientInputFunds(_, _) =>
                        "There are not enought funds to complete the transaction."
                      case TransactionSyntaxError.InvalidProofType(_, _) =>
                        "The type of the proof is invalid."
                      case TransactionSyntaxError.InvalidUpdateProposal(_) =>
                        "There are invalid update proposals in the output."
                      case _ =>
                        "Error."
                    }
                    .toList
                    .mkString(", ")
                )
              )
            case Right(_) => Sync[F].unit
          }
        } yield ()
      }

      override def proveTransactionFromParams(
        inputRes:  Resource[F, FileInputStream],
        keyFile:   String,
        password:  String,
        outputRes: Resource[F, FileOutputStream]
      ): F[Either[SimpleTransactionAlgebraError, Unit]] = {
        import org.plasmalabs.sdk.models.transaction.IoTransaction
        import cats.implicits._

        (for {
          ioTransaction <- inputRes.use(fis =>
            Sync[F]
              .blocking(IoTransaction.parseFrom(fis))
              .adaptErr(_ => InvalidProtobufFile("Invalid protobuf file"))
          )
          keyPair <- walletManagementUtils
            .loadKeys(
              keyFile,
              password
            )
          credentialer <- Sync[F]
            .delay(
              CredentiallerInterpreter
                .make[F](walletApi, walletStateApi, keyPair)
            )
          provedTransaction <- credentialer.prove(ioTransaction)
          _                 <- validateTx(ioTransaction)
          _ <- outputRes.use(fos =>
            Sync[F]
              .delay(provedTransaction.writeTo(fos))
          )
        } yield ()).attempt.map {
          case Right(_)                               => ().asRight
          case Left(e: SimpleTransactionAlgebraError) => e.asLeft
          case Left(e)                                => UnexpectedError(e.getMessage).asLeft
        }
      }

    }
}
