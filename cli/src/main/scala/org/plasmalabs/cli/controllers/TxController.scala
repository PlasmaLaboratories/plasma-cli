package org.plasmalabs.cli.controllers

import cats.effect.kernel.{Resource, Sync}
import org.plasmalabs.cli.impl.{CommonParserError, TransactionAlgebra, TxParserAlgebra}

import java.io.{FileInputStream, FileOutputStream}

class TxController[F[_]: Sync](
  txParserAlgebra: TxParserAlgebra[F],
  transactionOps:  TransactionAlgebra[F]
) {

  def createComplexTransaction(
    inputFile:  String,
    outputFile: String
  ): F[Either[String, String]] = {
    import cats.implicits._
    (for {
      eitherTx <- txParserAlgebra.parseComplexTransaction(
        Resource.make(
          Sync[F].delay(scala.io.Source.fromFile(inputFile))
        )(source => Sync[F].delay(source.close()))
      )
      tx <- Sync[F].fromEither(eitherTx)
      _ <- Resource
        .make(
          Sync[F]
            .delay(new FileOutputStream(outputFile))
        )(fos => Sync[F].delay(fos.close()))
        .use(fos => Sync[F].delay(tx.writeTo(fos)))
    } yield "Transaction created").attempt.map {
      case Right(_)                       => Right("Transaction created")
      case Left(value: CommonParserError) => Left(value.description)
      case Left(e)                        => Left(e.getMessage)
    }
  }

  def broadcastSimpleTransactionFromParams(
    provedTxFile: String
  ): F[Either[String, String]] = {
    import cats.implicits._
    transactionOps
      .broadcastSimpleTransactionFromParams(
        provedTxFile
      )
      .map {
        case Right(s)    => Right(s)
        case Left(value) => Left(value.description)
      }
  }

  def proveSimpleTransactionFromParams(
    inputFile:  String,
    keyFile:    String,
    password:   String,
    outputFile: String
  ): F[Either[String, String]] = {
    import cats.implicits._
    val inputRes = Resource
      .make {
        Sync[F].delay(new FileInputStream(inputFile))
      }(fos => Sync[F].delay(fos.close()))

    val outputRes = Resource
      .make(
        Sync[F].delay(new FileOutputStream(outputFile))
      )(fos => Sync[F].delay(fos.close()))

    transactionOps
      .proveSimpleTransactionFromParams(
        inputRes,
        keyFile,
        password,
        outputRes
      )
      .map {
        case Right(_)    => Right("Transaction successfully proved")
        case Left(value) => Left(value.description)
      }
  }

}
