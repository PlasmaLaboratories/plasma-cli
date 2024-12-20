package org.plasmalabs.cli.controllers

import cats.Id
import munit.FunSuite
import org.plasmalabs.sdk.dataApi.{FellowshipStorageAlgebra, WalletFellowship}

class FellowshipsControllerSpec extends FunSuite {

  test("Fellowship controller allows adding fellowships") {
    val controller = new FellowshipsController[Id](
      new FellowshipStorageAlgebra[Id] {
        override def addFellowship(
          walletEntity: WalletFellowship
        ): Id[Int] = 1

        override def findFellowships(): Id[List[WalletFellowship]] =
          List.empty
      }
    )
    assertEquals(
      controller.addFellowship("myNewFellowship"),
      Right("Fellowship myNewFellowship added successfully")
    )
  }

  test("List fellowship allows listing fellowships") {
    val controller = new FellowshipsController[Id](
      new FellowshipStorageAlgebra[Id] {
        override def addFellowship(
          walletEntity: WalletFellowship
        ): Id[Int] = 1

        override def findFellowships(): Id[List[WalletFellowship]] =
          List(
            WalletFellowship(1, "fellowship1"),
            WalletFellowship(2, "fellowship2")
          )
      }
    )
    assertEquals(
      controller.listFellowships(),
      Right(
        "X Coordinate\tFellowship Name\n" +
        "1\tfellowship1\n" +
        "2\tfellowship2".stripMargin
      )
    )
  }

}
