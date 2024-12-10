package org.plasmalabs.cli.controllers

import cats.effect.kernel.{Resource, Sync}
import org.plasmalabs.sdk.display.DisplayOps.DisplayTOps
import org.plasmalabs.sdk.display.transactionDisplay
import org.plasmalabs.sdk.models.transaction.IoTransaction

import java.io.FileInputStream

/**
 * A Simple Contoller, is a controller which no needs any algebra/ops to be defined.
 */
object SimpleController:

  def inspectTransaction[F[_]: Sync](
    inputFile: String
  ): F[Either[String, String]] = {
    import cats.implicits.*
    val inputRes = Resource
      .make(
        Sync[F]
          .delay(new FileInputStream(inputFile))
      )(fos => Sync[F].delay(fos.close()))
    (for {
      tx     <- inputRes.use(in => Sync[F].delay(IoTransaction.parseFrom(in)))
      output <- Sync[F].delay(tx.display)
    } yield output).attempt.map {
      case Right(output) => Right(output)
      case Left(e)       => Left(e.getMessage)
    }
  }

end SimpleController
