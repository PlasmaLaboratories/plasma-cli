package org.plasmalabs.strata.cli.controllers

import cats.Applicative
import org.plasmalabs.sdk.dataApi.{FellowshipStorageAlgebra, WalletFellowship}

class FellowshipsController[F[_]: Applicative](
    fellowshipStorageAlgebra: FellowshipStorageAlgebra[F]
) {

  def addFellowship(name: String): F[Either[String, String]] = {
    import cats.implicits._
    for {
      added <- fellowshipStorageAlgebra.addFellowship(WalletFellowship(0, name))
    } yield
      if (added == 1) Right(s"Fellowship $name added successfully")
      else Left("Failed to add fellowship")
  }

  def listFellowships(): F[Either[String, String]] = {
    import org.plasmalabs.strata.cli.views.WalletModelDisplayOps._
    import cats.implicits._
    fellowshipStorageAlgebra
      .findFellowships()
      .map(fellowships =>
        Right(
          displayWalletFellowshipHeader() + "\n" + fellowships
            .map(display)
            .mkString("\n")
        )
      )
  }

}
