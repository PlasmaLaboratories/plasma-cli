package co.topl.brambl.cli

import cats.effect.ExitCode
import cats.effect.IO
import co.topl.brambl.codecs.AddressCodecs.decodeAddress
import co.topl.brambl.utils.Encoding
import munit.CatsEffectSuite

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import scala.concurrent.duration.Duration

class MintingTests
    extends CatsEffectSuite
    with CommonTxOperations
    with AliceConstants
    with BobConstants
    with IntegrationTearDown
    with PolicyTemplates {

  val tmpDirectory = FunFixture[Path](
    setup = { _ =>
      val tmpDir = Paths.get(TMP_DIR).toFile()
      if (tmpDir.exists()) {
        Paths.get(TMP_DIR).toFile().listFiles().map(_.delete()).mkString("\n")
        Files.deleteIfExists(Paths.get(TMP_DIR))
      }
      Files.createDirectory(Paths.get("./tmp"))
    },
    teardown = { _ => () }
  )

  override val munitTimeout = Duration(180, "s")

  tmpDirectory.test("Move funds from genesis to alice") { _ =>
    import scala.concurrent.duration._
    assertIO(
      for {
        _ <- createWallet().run(aliceContext)
        _ <- IO.asyncForIO.timeout(
          (for {
            _ <- IO.println("Querying genesis to start")
            queryRes <- queryAccount("noparty", "genesis", Some(1))
              .run(aliceContext)
            _ <- IO.sleep(5.seconds)
          } yield queryRes)
            .iterateUntil(_ == ExitCode.Success),
          60.seconds
        )
        ALICE_TO_ADDRESS <- walletController(ALICE_WALLET).currentaddress()
        _ <- IO.println(s"Alice's address is $ALICE_TO_ADDRESS")
        _ <- IO.println("Moving funds from genesis to alice")
        _ <- assertIO(
          createSimpleTransactionToAddress(
            "noparty",
            "genesis",
            Some(1),
            ALICE_TO_ADDRESS.toOption.get,
            BASE_AMOUNT,
            ALICE_FIRST_TX_RAW
          ).run(aliceContext),
          ExitCode.Success
        )
        _ <- IO.sleep(5.seconds)
        _ <- assertIO(
          proveSimpleTransaction(
            ALICE_FIRST_TX_RAW,
            ALICE_FIRST_TX_PROVED
          ).run(aliceContext),
          ExitCode.Success
        )
        _ <- IO.sleep(5.seconds)
        _ <- assertIO(
          broadcastSimpleTx(ALICE_FIRST_TX_PROVED),
          ExitCode.Success
        )
        _ <- IO.println("Check alice's address (is contained in the change)")
        res <- IO.asyncForIO.timeout(
          (for {
            queryRes <- queryAccount("self", "default").run(aliceContext)
            _ <- IO.sleep(5.seconds)
          } yield queryRes)
            .iterateUntil(_ == ExitCode.Success),
          60.seconds
        )
      } yield res,
      ExitCode.Success
    )
  }

  test("Use alice's funds to mint a group") {
    import scala.concurrent.duration._
    assertIO(
      for {
        _ <- IO.println("Crate a group minting policy")
        ALICE_CURRENT_ADDRESS <- walletController(ALICE_WALLET)
          .currentaddress("self", "default", None)
        utxos <- genusQueryAlgebra
          .queryUtxo(
            decodeAddress(ALICE_CURRENT_ADDRESS.get).toOption.get
          )
          .map(_.filter(_.transactionOutput.value.value.isLvl))
        lvlUtxos = utxos.filter(_.transactionOutput.value.value.isLvl)
        _ <- IO.println(s"Alice's address is $ALICE_CURRENT_ADDRESS")
        aliceUtxoAddress = Encoding.encodeToBase58(
          lvlUtxos.head.outputAddress.id.value.toByteArray
        ) + "#" + lvlUtxos.head.outputAddress.index.toString
        _ <- createAliceGroupPolicy(
          ALICE_FIRST_GROUP_POLICY,
          "Alice Group",
          aliceUtxoAddress
        )
        _ <- assertIO(
          createSimpleGroupMintingTransaction(
            "self",
            "default",
            None,
            1,
            100,
            ALICE_FIRST_GROUP_POLICY,
            ALICE_FIRST_GROUP_MINTING_TX
          ).run(aliceContext),
          ExitCode.Success
        )
        _ <- IO.sleep(5.seconds)
        _ <- assertIO(
          proveSimpleTransaction(
            ALICE_FIRST_GROUP_MINTING_TX,
            ALICE_FIRST_GROUP_MINTING_TX_PROVED
          ).run(aliceContext),
          ExitCode.Success
        )
        _ <- IO.sleep(5.seconds)
        _ <- assertIO(
          broadcastSimpleTx(ALICE_FIRST_GROUP_MINTING_TX_PROVED),
          ExitCode.Success
        )
        _ <- IO.sleep(5.seconds)
        _ <- IO.println(
          "Check change  account for from alice's wallet, group tokens"
        )
        res <- IO.asyncForIO.timeout(
          (for {
            _ <- IO.println("Querying alice's change account")
            queryRes <- queryAccountGroupTokens("self", "default").run(
              aliceContext
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

  test("Use alice's funds to mint a series") {
    import scala.concurrent.duration._
    assertIO(
      for {
        _ <- IO.println("Crate a series minting policy")
        ALICE_CURRENT_ADDRESS <- walletController(ALICE_WALLET)
          .currentaddress("self", "default", None)
        utxos <- genusQueryAlgebra
          .queryUtxo(
            decodeAddress(ALICE_CURRENT_ADDRESS.get).toOption.get
          )
          .map(_.filter(_.transactionOutput.value.value.isLvl))
        lvlUtxos = utxos.filter(_.transactionOutput.value.value.isLvl)
        _ <- IO.println(s"Alice's address is $ALICE_CURRENT_ADDRESS")
        aliceUtxoAddress = Encoding.encodeToBase58(
          lvlUtxos.head.outputAddress.id.value.toByteArray
        ) + "#" + lvlUtxos.head.outputAddress.index.toString
        _ <- createAliceSeriesPolicy(
          ALICE_FIRST_SERIES_POLICY,
          "Alice Series",
          "group-and-series",
          "liquid",
          aliceUtxoAddress
        )
        _ <- assertIO(
          createSimpleSeriesMintingTransaction(
            "self",
            "default",
            None,
            1,
            100,
            ALICE_FIRST_SERIES_POLICY,
            ALICE_FIRST_SERIES_MINTING_TX
          ).run(aliceContext),
          ExitCode.Success
        )
        _ <- IO.sleep(5.seconds)
        _ <- assertIO(
          proveSimpleTransaction(
            ALICE_FIRST_SERIES_MINTING_TX,
            ALICE_FIRST_SERIES_MINTING_TX_PROVED
          ).run(aliceContext),
          ExitCode.Success
        )
        _ <- IO.sleep(5.seconds)
        _ <- assertIO(
          broadcastSimpleTx(ALICE_FIRST_SERIES_MINTING_TX_PROVED),
          ExitCode.Success
        )
        _ <- IO.sleep(5.seconds)
        _ <- IO.println(
          "Check change  account for from alice's wallet, expected group and series tokens"
        )
        res <- IO.asyncForIO.timeout(
          (for {
            _ <- IO.println("Querying alice's change account")
            queryRes <- queryAccountSeriesTokens("self", "default").run(
              aliceContext
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

  test("Use alice's funds to mint an asset") {
    import scala.concurrent.duration._
    assertIO(
      for {
        _ <- IO.println("Crate an asset minting statement")
        ALICE_CURRENT_ADDRESS <- walletController(ALICE_WALLET)
          .currentaddress("self", "default", None)
        utxos <- genusQueryAlgebra
          .queryUtxo(
            decodeAddress(ALICE_CURRENT_ADDRESS.get).toOption.get
          )
        groupTokenUtxo = utxos.filter(_.transactionOutput.value.value.isGroup)
        _ <- IO.println(s"Alice's address is $ALICE_CURRENT_ADDRESS")
        groupTokenUtxoAddress = Encoding.encodeToBase58(
          groupTokenUtxo.head.outputAddress.id.value.toByteArray
        ) + "#" + groupTokenUtxo.head.outputAddress.index.toString
        seriesTokenUtxo = utxos.filter(_.transactionOutput.value.value.isSeries)
        _ <- IO.println(s"Alice's address is $ALICE_CURRENT_ADDRESS")
        seriesTokenUtxoAddress = Encoding.encodeToBase58(
          seriesTokenUtxo.head.outputAddress.id.value.toByteArray
        ) + "#" + seriesTokenUtxo.head.outputAddress.index.toString
        _ <- createAliceAssetMintingStatement(
          ALICE_FIRST_ASSET_MINTING_STATEMENT,
          groupTokenUtxoAddress,
          seriesTokenUtxoAddress,
          1000
        )
        _ <- createAliceEphemeralMetadata(
          ALICE_FIRST_ASSET_MINTING_METADATA,
          "http://topl.co",
          "http://topl.co/image.png",
          42
        )
        _ <- assertIO(
          createSimpleAssetMintingTransaction(
            "self",
            "default",
            None,
            100,
            ALICE_FIRST_ASSET_MINTING_STATEMENT,
            ALICE_FIRST_ASSET_MINTING_TX,
            ALICE_FIRST_ASSET_MINTING_METADATA
          ).run(aliceContext),
          ExitCode.Success
        )
        _ <- IO.sleep(5.seconds)
        _ <- assertIO(
          proveSimpleTransaction(
            ALICE_FIRST_ASSET_MINTING_TX,
            ALICE_FIRST_ASSET_MINTING_TX_PROVED
          ).run(aliceContext),
          ExitCode.Success
        )
        _ <- IO.sleep(5.seconds)
        _ <- assertIO(
          broadcastSimpleTx(ALICE_FIRST_ASSET_MINTING_TX_PROVED),
          ExitCode.Success
        )
        _ <- IO.sleep(5.seconds)
        _ <- IO.println(
          "Check change  account for from alice's wallet, expected a new asset"
        )
        res <- IO.asyncForIO.timeout(
          (for {
            _ <- IO.println("Querying alice's change account")
            queryRes <- queryAccountAssetTokens("self", "default").run(
              aliceContext
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