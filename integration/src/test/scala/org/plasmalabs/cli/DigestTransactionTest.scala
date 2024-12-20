package org.plasmalabs.cli

import cats.effect.{ExitCode, IO}
import munit.CatsEffectSuite

import java.nio.file.{Files, Paths}
import scala.concurrent.duration.Duration

class DigestTransactionTest
    extends CatsEffectSuite
    with CommonFunctions
    with AliceConstants
    with BobConstants
    with CommonTxOperations
    with CommonFunFixture {

  override val munitIOTimeout: Duration = Duration(180, "s")

  tmpDirectory.test("Move funds from genesis to alice") { _ =>
    assertIO(
      moveFundsFromGenesisToAlice(),
      ExitCode.Success
    )
  }

  test("Move funds from alice to digest locked account") {
    import scala.concurrent.duration.*
    assertIO(
      for {
        _ <- IO.println("Create a wallet for bob")
        _ <- assertIO(createWallet().run(bobContext), ExitCode.Success)
        _ <- IO.println("Add bob's digest fellowship to bob's wallet")
        _ <- assertIO(
          addFellowshipToWallet("bob_digest_fellowship").run(bobContext),
          ExitCode.Success
        )
        _ <- IO.println("Add a template to bob's wallet")
        _ <- assertIO(
          addTemplateToWallet(
            "digest_template",
            "threshold(1, sign(0) and sha256(ee15b31e49931db6551ed8a82f1422ce5a5a8debabe8e81a724c88f79996d0df))"
          ).run(bobContext),
          ExitCode.Success
        )
        _ <- IO.println("Importing VK to bob's wallet")
        _ <- assertIO(
          exportVk("bob_digest_fellowship", "digest_template", EMPTY_VK).run(
            bobContext
          ),
          ExitCode.Success
        )
        _ <- assertIO(
          importVk("bob_digest_fellowship", "digest_template", EMPTY_VK).run(
            bobContext
          ),
          ExitCode.Success
        )
        _ <- IO(Files.delete(Paths.get(EMPTY_VK)))
        BOB_DIGEST_ADDRESS <- walletController(BOB_WALLET)
          .currentaddress("bob_digest_fellowship", "digest_template", None)
        _ <- IO.println("Bob's digest address: " + BOB_DIGEST_ADDRESS)
        _ <- IO.println("Moving funds (500 LVLs) from alice to bob digest")
        _ <- assertIO(
          createSimpleTransactionToAddress(
            "self",
            "default",
            None,
            None,
            None,
            None,
            BOB_DIGEST_ADDRESS.get,
            600,
            BASE_FEE,
            ALICE_SECOND_TX_RAW,
            TokenType.lvl,
            None,
            None
          ).run(aliceContext),
          ExitCode.Success
        )
        _ <- assertIO(
          proveSimpleTransaction(
            ALICE_SECOND_TX_RAW,
            ALICE_SECOND_TX_PROVED
          ).run(aliceContext),
          ExitCode.Success
        )
        _ <- assertIO(
          broadcastSimpleTx(ALICE_SECOND_TX_PROVED),
          ExitCode.Success
        )
        _ <- IO.println(
          "Check digest account from bob's wallet, lvl tokens"
        )
        res <- IO.asyncForIO.timeout(
          (for {
            _ <- IO.println("Querying bob's digest")
            queryRes <- queryAccount("bob_digest_fellowship", "digest_template")
              .run(bobContext)
            _ <- IO.sleep(5.seconds)
          } yield queryRes)
            .iterateUntil(_ == ExitCode.Success),
          240.seconds
        )
      } yield res,
      ExitCode.Success
    )
  }

  test("Move funds from digest locked account to bob's normal account") {
    import scala.concurrent.duration.*
    assertIO(
      for {
        _ <- assertIO(
          addSecret("topl-secret", "sha256").run(bobContext),
          ExitCode.Success
        )
        _ <- assertIO(
          createSimpleTransactionToCartesianIdx(
            "bob_digest_fellowship",
            "digest_template",
            None,
            None,
            None,
            None,
            "self",
            "default",
            500,
            BASE_FEE,
            BOB_FIRST_TX_RAW,
            TokenType.lvl,
            None,
            None
          ).run(bobContext),
          ExitCode.Success
        )
        _ <- assertIO(
          proveSimpleTransaction(
            BOB_FIRST_TX_RAW,
            BOB_FIRST_TX_PROVED
          ).run(bobContext),
          ExitCode.Success
        )
        _ <- assertIO(
          broadcastSimpleTx(BOB_FIRST_TX_PROVED),
          ExitCode.Success
        )
        _ <- IO.println(
          "Check digest account from bob's wallet, lvl tokens"
        )
        res <- IO.asyncForIO.timeout(
          (for {
            _        <- IO.println("Querying bob's address")
            queryRes <- queryAccount("self", "default").run(bobContext)
            _        <- IO.sleep(5.seconds)
          } yield queryRes)
            .iterateUntil(_ == ExitCode.Success),
          240.seconds
        )
      } yield res,
      ExitCode.Success
    )
  }

}
