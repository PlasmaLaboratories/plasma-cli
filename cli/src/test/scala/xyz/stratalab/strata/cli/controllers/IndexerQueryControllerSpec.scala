package xyz.stratalab.strata.cli.controllers

import cats.Monad
import cats.effect.IO
import xyz.stratalab.strata.cli.mockbase.BaseWalletStateAlgebra
import xyz.stratalab.strata.cli.modules.DummyObjects
import xyz.stratalab.sdk.dataApi.IndexerQueryAlgebra
import xyz.stratalab.sdk.display.DisplayOps.DisplayTOps
import xyz.stratalab.sdk.models.LockAddress
import xyz.stratalab.indexer.services.{Txo, TxoState}
import munit.CatsEffectSuite

class IndexerQueryControllerSpec extends CatsEffectSuite with DummyObjects {

  def makeWalletStateAlgebraMock[F[_]: Monad] = new BaseWalletStateAlgebra[F] {

    override def getAddress(
        fellowship: String,
        template: String,
        interaction: Option[Int]
    ): F[Option[String]] = Monad[F].pure(None)
  }
  def makeWalletStateAlgebraMockWithAddress[F[_]: Monad] =
    new BaseWalletStateAlgebra[F] {

      override def getAddress(
          fellowship: String,
          template: String,
          interaction: Option[Int]
      ): F[Option[String]] = Monad[F].pure(
        Some("ptetP7jshHVrEKqDRdKAZtuybPZoMWTKKM2ngaJ7L5iZnxP5BprDB3hGJEFr")
      )
    }

  def makeIndexerQueryAlgebraMock[F[_]: Monad] = new IndexerQueryAlgebra[F] {

    override def queryUtxo(
        fromAddress: LockAddress,
        txoState: TxoState
    ): F[Seq[Txo]] = Monad[F].pure(Seq.empty)

  }

  test(
    "queryUtxoFromParams should return an error if the address is not there"
  ) {
    val walletStateAlgebra = makeWalletStateAlgebraMock[IO]
    val indexerQueryAlgebra = makeIndexerQueryAlgebraMock[IO]
    val indexerQueryController =
      new IndexerQueryController[IO](walletStateAlgebra, indexerQueryAlgebra)
    val result =
      indexerQueryController.queryUtxoFromParams(
        None,
        "fellowship",
        "template",
        None
      )
    assertIO(result, Left("Address not found"))
  }

  test(
    "queryUtxoFromParams should return a formatted string if the address is there"
  ) {
    val walletStateAlgebra = makeWalletStateAlgebraMockWithAddress[IO]
    val indexerQueryAlgebra = makeIndexerQueryAlgebraMockWithOneAddress[IO]
    val indexerQueryController =
      new IndexerQueryController[IO](walletStateAlgebra, indexerQueryAlgebra)
    val result =
      indexerQueryController.queryUtxoFromParams(
        None,
        "fellowship",
        "template",
        None
      )
    assertIO(
      result,
      Right(txo01.display)
    )
  }
}
