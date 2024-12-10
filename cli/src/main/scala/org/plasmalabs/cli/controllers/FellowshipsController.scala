package org.plasmalabs.cli.controllers

import cats.Applicative
import cats.implicits.*
import org.plasmalabs.cli.views.WalletModelDisplayOps.*
import org.plasmalabs.sdk.dataApi.{FellowshipStorageAlgebra, WalletFellowship}

class FellowshipsController[F[_]: Applicative](
  fellowshipStorageAlgebra: FellowshipStorageAlgebra[F]
):

  def addFellowship(name: String): F[Either[String, String]] =
    for {
      added <- fellowshipStorageAlgebra.addFellowship(WalletFellowship(0, name))
    } yield
      if (added == 1) Right(s"Fellowship $name added successfully")
      else Left("Failed to add fellowship")

  def listFellowships(): F[Either[String, String]] =
    fellowshipStorageAlgebra
      .findFellowships()
      .map(fellowships =>
        Right(
          displayWalletFellowshipHeader() + "\n" + fellowships
            .map(display)
            .mkString("\n")
        )
      )

end FellowshipsController
