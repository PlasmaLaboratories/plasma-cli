package org.plasmalabs.cli.controllers

import cats.Monad
import cats.effect.IO
import cats.effect.kernel.Sync
import munit.CatsEffectSuite
import org.plasmalabs.cli.impl.SimpleMintingAlgebra
import org.plasmalabs.cli.mockbase.BaseWalletStateAlgebra
import org.plasmalabs.cli.modules.{DummyObjects, ParserModule, SimpleMintingAlgebraModule}
import org.plasmalabs.quivr.models.Proposition
import org.plasmalabs.sdk.constants.NetworkConstants
import org.plasmalabs.sdk.models.Indices
import org.plasmalabs.sdk.models.box.{Challenge, Lock}

import java.io.File

class SimpleMintingControllerSpec
    extends CatsEffectSuite
    with ParserModule.Group
    with ParserModule.Series
    with ParserModule.Ams
    with SimpleMintingAlgebraModule
    with DummyObjects {

  private def makeWalletStateAlgebraMockWithAddress[F[_]: Monad] =
    new BaseWalletStateAlgebra[F] {

      override def getCurrentIndicesForFunds(
        fellowship:  String,
        template:    String,
        interaction: Option[Int]
      ): F[Option[Indices]] = Monad[F].pure(
        Some(Indices(1, 1, 1))
      )

      override def getNextIndicesForFunds(
        fellowship: String,
        template:   String
      ): F[Option[Indices]] = Monad[F].pure(
        Some(Indices(1, 1, 1))
      )

      override def updateWalletState(
        lockPredicate: String,
        lockAddress:   String,
        routine:       Option[String],
        vk:            Option[String],
        indices:       Indices
      ): F[Unit] = Monad[F].pure(())

      override def getLock(
        fellowship: String,
        template:   String,
        nextState:  Int
      ): F[Option[Lock]] =
        Monad[F].pure(
          Some(
            Lock().withPredicate(
              Lock.Predicate.of(
                Seq(
                  Challenge.defaultInstance.withProposition(
                    Challenge.Proposition.Revealed(
                      Proposition.of(
                        Proposition.Value.Locked(Proposition.Locked())
                      )
                    )
                  )
                ),
                1
              )
            )
          )
        )

      override def getLockByIndex(indices: Indices): F[Option[Lock.Predicate]] =
        Monad[F].pure(
          Some(
            Lock.Predicate.of(
              Seq(
                Challenge.defaultInstance.withProposition(
                  Challenge.Proposition.Revealed(
                    Proposition.of(
                      Proposition.Value.Locked(Proposition.Locked())
                    )
                  )
                )
              ),
              1
            )
          )
        )

    }

  val controllerUnderTest = new SimpleMintingController(
    groupPolicyParser(NetworkConstants.PRIVATE_NETWORK_ID),
    seriesPolicyParser(NetworkConstants.PRIVATE_NETWORK_ID),
    assetMintingStatementParser(NetworkConstants.PRIVATE_NETWORK_ID),
    simpleMintingAlgebra()
  )

  private def simpleMintingAlgebra(
  ) = SimpleMintingAlgebra.make[IO](
    Sync[IO],
    walletApi,
    makeWalletStateAlgebraMockWithAddress[IO],
    walletManagementUtils,
    transactionBuilderApi(
      NetworkConstants.PRIVATE_NETWORK_ID,
      NetworkConstants.MAIN_LEDGER_ID
    ),
    makeIndexerQueryAlgebraMockWithAddress
  )

  test(
    "createSimpleGroupMintingTransactionFromParams should create a minting transaction"
  ) {
    assertIO(
      controllerUnderTest.createSimpleGroupMintingTransactionFromParams(
        "src/test/resources/valid_group_policy.yaml",
        "src/test/resources/keyfile.json",
        "test",
        "self",
        "default",
        None,
        1L,
        100,
        "target/transaction.pbuf"
      ),
      Right("Transaction successfully created")
    )
  }

  test(
    "createSimpleSeriesMintingTransactionFromParams should create a minting transaction"
  ) {
    assertIO(
      controllerUnderTest.createSimpleSeriesMintingTransactionFromParams(
        "src/test/resources/valid_series_policy.yaml",
        "src/test/resources/keyfile.json",
        "test",
        "self",
        "default",
        None,
        1L,
        100,
        "target/transaction_series_mint.pbuf"
      ),
      Right("Transaction successfully created")
    )
  }

  test(
    "createSimpleAssetMintingTransactionFromParams should create a minting transaction"
  ) {
    assertIO(
      controllerUnderTest.createSimpleAssetMintingTransactionFromParams(
        "src/test/resources/valid_asset_minting_statement.yaml",
        "src/test/resources/keyfile.json",
        "test",
        "self",
        "default",
        None,
        100,
        None,
        None,
        "target/transaction_asset_mint.pbuf"
      ),
      Right("Transaction successfully created")
    )
  }

  test(
    "createSimpleAssetMintingTransactionFromParams should create a minting transaction with ephemeral and permanent metadata"
  ) {
    assertIO(
      controllerUnderTest.createSimpleAssetMintingTransactionFromParams(
        "src/test/resources/valid_asset_minting_statement_metadata.yaml",
        "src/test/resources/keyfile.json",
        "test",
        "self",
        "default",
        None,
        100,
        Some(new File("src/test/resources/simple_metadata.json")),
        None,
        "target/transaction_asset_metadata_mint.pbuf"
      ),
      Right("Transaction successfully created")
    )
  }

  test(
    "createSimpleSeriesMintingTransactionFromParams should elegantly fail if the policy file is invalid: quantity descriptor"
  ) {
    assertIO(
      controllerUnderTest.createSimpleSeriesMintingTransactionFromParams(
        "src/test/resources/invalid_series_policy_quantity_descriptor.yaml",
        "src/test/resources/keyfile.json",
        "test",
        "self",
        "default",
        None,
        1L,
        100,
        "target/transaction_series_mint.pbuf"
      ),
      Left("Error parsing series policy: Invalid quantity descriptor: standard")
    )
  }

  test(
    "createSimpleSeriesMintingTransactionFromParams should elegantly fail if the policy file is invalid: fungibility"
  ) {
    assertIO(
      controllerUnderTest.createSimpleSeriesMintingTransactionFromParams(
        "src/test/resources/invalid_series_policy_fungibility.yaml",
        "src/test/resources/keyfile.json",
        "test",
        "self",
        "default",
        None,
        1L,
        100,
        "target/transaction_series_mint.pbuf"
      ),
      Left("Error parsing series policy: Invalid fungibility: fungus")
    )
  }

  test(
    "createSimpleGroupMintingTransactionFromParams should elegantly fail if the policy file is invalid"
  ) {
    assertIO(
      controllerUnderTest.createSimpleGroupMintingTransactionFromParams(
        "src/test/resources/invalid_group_policy.yaml",
        "src/test/resources/keyfile.json",
        "test",
        "self",
        "default",
        None,
        1L,
        100,
        "target/transaction.pbuf"
      ),
      Left(
        "Error parsing group policy: DecodingFailure(Attempt to decode value on failed cursor, List(DownField(registrationUtxo)))"
      )
    )
  }
}
