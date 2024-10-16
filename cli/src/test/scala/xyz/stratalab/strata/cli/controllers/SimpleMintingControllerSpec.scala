package xyz.stratalab.strata.cli.controllers

import cats.effect.IO
import cats.effect.kernel.Sync
import xyz.stratalab.strata.cli.impl.AssetStatementParserModule
import xyz.stratalab.strata.cli.impl.GroupPolicyParserModule
import xyz.stratalab.strata.cli.impl.SeriesPolicyParserModule
import xyz.stratalab.strata.cli.impl.SimpleMintingAlgebra
import xyz.stratalab.strata.cli.modules.DummyObjects
import xyz.stratalab.strata.cli.modules.SimpleMintingAlgebraModule
import xyz.stratalab.sdk.constants.NetworkConstants
import munit.CatsEffectSuite
import java.io.File
import cats.Monad
import xyz.stratalab.strata.cli.mockbase.BaseWalletStateAlgebra
import xyz.stratalab.sdk.models.Indices
import xyz.stratalab.sdk.models.box.Lock
import xyz.stratalab.sdk.models.box.Challenge
import quivr.models.Proposition
import quivr.models.KeyPair

class SimpleMintingControllerSpec
    extends CatsEffectSuite
    with GroupPolicyParserModule
    with SeriesPolicyParserModule
    with AssetStatementParserModule
    with SimpleMintingAlgebraModule
    with DummyObjects {

  def makeWalletStateAlgebraMockWithAddress[F[_]: Monad] =
    new BaseWalletStateAlgebra[F] {

      override def getCurrentIndicesForFunds(
          fellowship: String,
          template: String,
          interaction: Option[Int]
      ): F[Option[Indices]] = Monad[F].pure(
        Some(Indices(1, 1, 1))
      )

      override def getNextIndicesForFunds(
          fellowship: String,
          template: String
      ): F[Option[Indices]] = Monad[F].pure(
        Some(Indices(1, 1, 1))
      )

      override def updateWalletState(
          lockPredicate: String,
          lockAddress: String,
          routine: Option[String],
          vk: Option[String],
          indices: Indices
      ): F[Unit] = Monad[F].pure(())

      override def getLock(
          fellowship: String,
          template: String,
          nextState: Int
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
    groupPolicyParserAlgebra(NetworkConstants.PRIVATE_NETWORK_ID),
    seriesPolicyParserAlgebra(NetworkConstants.PRIVATE_NETWORK_ID),
    assetMintingStatementParserAlgebra(NetworkConstants.PRIVATE_NETWORK_ID),
    simpleMintingAlgebra()
  )

  def simpleMintingAlgebra(
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
