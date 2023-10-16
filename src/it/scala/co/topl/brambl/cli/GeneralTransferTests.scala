package co.topl.brambl.cli
import cats.effect.ExitCode
import munit.CatsEffectSuite
import cats.effect.IO
import co.topl.brambl.codecs.AddressCodecs.decodeAddress
import co.topl.brambl.utils.Encoding
import scala.concurrent.duration.Duration

class GeneralTransferTests
    extends CatsEffectSuite
    with CommonFunctions
    with MintingFunctions
    with CommonTxOperations
    with AliceConstants
    with BobConstants
    with IntegrationTearDown {

  override val munitTimeout = Duration(180, "s")

  tmpDirectory.test("Move funds from genesis to alice") { _ =>
    assertIO(
      for {
        _ <- moveFundsFromGenesisToAlice()
        res <- mintGroup()
      } yield res,
      ExitCode.Success
    )
  }

  test("Move group asset from alice to bob") {
    import scala.concurrent.duration._
    assertIO(
      for {
        _ <- IO.println("Create a wallet for bob")
        _ <- assertIO(createWallet().run(bobContext), ExitCode.Success)
        ALICE_CURRENT_ADDRESS <- walletController(ALICE_WALLET)
          .currentaddress("self", "default", None)
        BOB_CURRENT_ADDRESS <- walletController(BOB_WALLET)
          .currentaddress("self", "default", None)
        utxos <- genusQueryAlgebra
          .queryUtxo(
            decodeAddress(ALICE_CURRENT_ADDRESS.get).toOption.get
          )
          .map(_.filter(_.transactionOutput.value.value.isGroup))
        _ <- assertIO(
          createSimpleTransactionToAddress(
            "self",
            "default",
            None,
            BOB_CURRENT_ADDRESS.get,
            1,
            BASE_FEE,
            ALICE_TRANSFER_GROUP_TX_RAW,
            TokenType.group,
            utxos.head.transactionOutput.value.value.group
              .map(x => Encoding.encodeToHex(x.groupId.value.toByteArray()))
          ).run(aliceContext),
          ExitCode.Success
        )
        _ <- IO.sleep(5.seconds)
        _ <- assertIO(
          proveSimpleTransaction(
            ALICE_TRANSFER_GROUP_TX_RAW,
            ALICE_TRANSFER_GROUP_TX_PROVED
          ).run(aliceContext),
          ExitCode.Success
        )
        _ <- IO.sleep(5.seconds)
        _ <- assertIO(
          broadcastSimpleTx(ALICE_TRANSFER_GROUP_TX_PROVED),
          ExitCode.Success
        )
        _ <- IO.sleep(5.seconds)
        _ <- IO.println(
          "Check change  account for from bob's wallet, group tokens"
        )
        res <- IO.asyncForIO.timeout(
          (for {
            _ <- IO.println("Querying bob's account")
            queryRes <- queryAccountGroupTokens("self", "default").run(
              bobContext
            )
            _ <- IO.sleep(5.seconds)
          } yield queryRes)
            .iterateUntil(_ == ExitCode.Success),
          60.seconds
        )
      } yield res,
      ExitCode.Success
    )
  }

  test("Send Wallet Change back to HeightLock") {
    assertIO(
      tearDown(aliceContext),
      ExitCode.Success
    )
  }

}