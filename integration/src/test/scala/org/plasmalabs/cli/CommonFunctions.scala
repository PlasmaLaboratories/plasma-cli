package org.plasmalabs.cli

import cats.effect.{ExitCode, IO}
import munit.CatsEffectSuite

import scala.concurrent.duration.*

trait CommonFunctions extends PolicyTemplates {

  self: CatsEffectSuite with CommonTxOperations with AliceConstants with BobConstants =>

  def moveFundsFromGenesisToAlice(secure: Boolean = false) =
    for {
      _ <- createWallet().run(aliceContext)
      _ <- IO.asyncForIO.timeout(
        (for {
          _ <- IO.println("Querying genesis to start")
          queryRes <- queryAccount("nofellowship", "genesis", Some(1), secure)
            .run(aliceContext)
          _ <- IO.sleep(5.seconds)
        } yield queryRes)
          .iterateUntil(_ == ExitCode.Success),
        240.seconds
      )
      ALICE_TO_ADDRESS <- walletController(ALICE_WALLET).currentaddress(
        "self",
        "default",
        None
      )
      _ <- IO.println(s"Alice's address is $ALICE_TO_ADDRESS")
      _ <- IO.println("Moving funds from genesis to alice")
      _ <- assertIO(
        createSimpleTransactionToAddress(
          "nofellowship",
          "genesis",
          Some(1),
          Some("nofellowship"),
          Some("genesis"),
          Some(1),
          ALICE_TO_ADDRESS.get,
          BASE_AMOUNT,
          BASE_FEE,
          ALICE_FIRST_TX_RAW,
          TokenType.lvl,
          None,
          None,
          secure
        ).run(aliceContext),
        ExitCode.Success
      )
      _ <- assertIO(
        proveSimpleTransaction(
          ALICE_FIRST_TX_RAW,
          ALICE_FIRST_TX_PROVED
        ).run(aliceContext),
        ExitCode.Success
      )
      _ <- assertIO(
        broadcastSimpleTx(ALICE_FIRST_TX_PROVED, secure),
        ExitCode.Success
      )
      _ <- IO.println("Check alice's address (is contained in the change)")
      res <- IO.asyncForIO.timeout(
        (for {
          queryRes <- queryAccount("self", "default", None, secure).run(
            aliceContext
          )
          _ <- IO.sleep(5.seconds)
        } yield queryRes)
          .iterateUntil(_ == ExitCode.Success),
        240.seconds
      )
    } yield res

}
