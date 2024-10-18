package org.plasmalabs.strata.cli.impl

import org.plasmalabs.sdk.dataApi.WalletStateAlgebra
import cats.Monad
import org.plasmalabs.sdk.models.Indices
import org.plasmalabs.sdk.models.box.Lock

trait WalletApiHelpers[F[_]] {

  import cats.implicits._

  val wsa: WalletStateAlgebra[F]

  implicit val m: Monad[F]

  def getCurrentIndices(
      fromFellowship: String,
      fromTemplate: String,
      someFromInteraction: Option[Int]
  ) = wsa.getCurrentIndicesForFunds(
    fromFellowship,
    fromTemplate,
    someFromInteraction
  )

  def getPredicateFundsToUnlock(someIndices: Option[Indices]) =
    someIndices
      .map(currentIndices => wsa.getLockByIndex(currentIndices))
      .sequence
      .map(_.flatten.map(Lock().withPredicate(_)))

  def getNextIndices(
      fromFellowship: String,
      fromTemplate: String
  ) =
    wsa.getNextIndicesForFunds(
      if (fromFellowship == "nofellowship") "self" else fromFellowship,
      if (fromFellowship == "nofellowship") "default"
      else fromTemplate
    )

  def getChangeLockPredicate(
      someNextIndices: Option[Indices],
      fromFellowship: String,
      fromTemplate: String
  ) =
    someNextIndices
      .map(idx =>
        wsa.getLock(
          if (fromFellowship == "nofellowship") "self" else fromFellowship,
          if (fromFellowship == "nofellowship") "default"
          else fromTemplate,
          idx.z
        )
      )
      .sequence
      .map(_.flatten)

}
