package org.plasmalabs.cli.mockbase

import org.plasmalabs.sdk.builders.BuilderError
import org.plasmalabs.sdk.builders.TransactionBuilderApi
import org.plasmalabs.sdk.models._
import org.plasmalabs.sdk.models.box.Attestation
import org.plasmalabs.sdk.models.box.FungibilityType
import org.plasmalabs.sdk.models.box.Lock
import org.plasmalabs.sdk.models.box.QuantityDescriptorType
import org.plasmalabs.sdk.models.transaction.IoTransaction
import org.plasmalabs.sdk.models.transaction.UnspentTransactionOutput
import org.plasmalabs.sdk.syntax.ValueTypeIdentifier
import org.plasmalabs.indexer.services.Txo
import com.google.protobuf.ByteString
import com.google.protobuf.struct.Struct
import org.plasmalabs.quivr.models.Int128
import org.plasmalabs.sdk.models.TransactionOutputAddress

class BaseTransactionBuilderApi[F[_]] extends TransactionBuilderApi[F] {

  override def buildTransferAllTransaction(
    txos:                 Seq[Txo],
    lockPredicateFrom:    Lock.Predicate,
    recipientLockAddress: LockAddress,
    changeLockAddress:    LockAddress,
    fee:                  Long,
    tokenIdentifier:      Option[ValueTypeIdentifier]
  ): F[Either[BuilderError, IoTransaction]] = ???

  override def buildTransferAmountTransaction(
    tokenIdentifier:      ValueTypeIdentifier,
    txos:                 Seq[Txo],
    lockPredicateFrom:    Lock.Predicate,
    amount:               Long,
    recipientLockAddress: LockAddress,
    changeLockAddress:    LockAddress,
    fee:                  Long
  ): F[Either[BuilderError, IoTransaction]] = ???

  override def groupOutput(
    lockAddress: LockAddress,
    quantity:    Int128,
    groupId:     GroupId,
    fixedSeries: Option[SeriesId]
  ): F[UnspentTransactionOutput] = ???

  override def seriesOutput(
    lockAddress:        LockAddress,
    quantity:           Int128,
    seriesId:           SeriesId,
    tokenSupply:        Option[Int],
    fungibility:        FungibilityType,
    quantityDescriptor: QuantityDescriptorType
  ): F[UnspentTransactionOutput] = ???

  override def assetOutput(
    lockAddress:            LockAddress,
    quantity:               Int128,
    groupId:                GroupId,
    seriesId:               SeriesId,
    fungibilityType:        FungibilityType,
    quantityDescriptorType: QuantityDescriptorType,
    metadata:               Option[Struct],
    commitment:             Option[ByteString]
  ): F[UnspentTransactionOutput] = ???

  override def buildGroupMintingTransaction(
    txos:              Seq[Txo],
    lockPredicateFrom: Lock.Predicate,
    groupPolicy:       GroupPolicy,
    quantityToMint:    Long,
    mintedAddress:     LockAddress,
    changeAddress:     LockAddress,
    fee:               Long
  ): F[Either[BuilderError, IoTransaction]] = ???

  override def buildSeriesMintingTransaction(
    txos:              Seq[Txo],
    lockPredicateFrom: Lock.Predicate,
    seriesPolicy:      SeriesPolicy,
    quantityToMint:    Long,
    mintedAddress:     LockAddress,
    changeAddress:     LockAddress,
    fee:               Long
  ): F[Either[BuilderError, IoTransaction]] = ???

  override def buildAssetMintingTransaction(
    mintingStatement:       AssetMintingStatement,
    txos:                   Seq[Txo],
    locks:                  Map[LockAddress, Lock.Predicate],
    fee:                    Long,
    mintedAssetLockAddress: LockAddress,
    changeAddress:          LockAddress,
    ephemeralMetadata:      Option[Struct],
    commitment:             Option[ByteString]
  ): F[Either[BuilderError, IoTransaction]] = ???

  override def buildAssetMergeTransaction(
    utxosToMerge:           Seq[TransactionOutputAddress],
    txos:                   Seq[Txo],
    locks:                  Map[LockAddress, Lock.Predicate],
    fee:                    Long,
    mergedAssetLockAddress: LockAddress,
    changeAddress:          LockAddress,
    ephemeralMetadata:      Option[Struct],
    commitment:             Option[ByteString]
  ): F[Either[BuilderError, IoTransaction]] = ???

  override def unprovenAttestation(
    lockPredicate: Lock.Predicate
  ): F[Attestation] = ???

  override def lockAddress(lock: Lock): F[LockAddress] = ???

  override def lvlOutput(
    predicate: Lock.Predicate,
    amount:    Int128
  ): F[UnspentTransactionOutput] = ???

  override def lvlOutput(
    lockAddress: LockAddress,
    amount:      Int128
  ): F[UnspentTransactionOutput] = ???

  override def datum(
    groupPolicies:       Seq[GroupPolicy],
    seriesPolicies:      Seq[SeriesPolicy],
    mintingStatements:   Seq[AssetMintingStatement],
    mergingStatements:   Seq[AssetMergingStatement],
    splittingStatements: Seq[AssetSplittingStatement]
  ): F[Datum.IoTransaction] = ???

  override def buildSimpleLvlTransaction(
    lvlTxos:                Seq[Txo],
    lockPredicateFrom:      Lock.Predicate,
    lockPredicateForChange: Lock.Predicate,
    recipientLockAddress:   LockAddress,
    amount:                 Long
  ): F[IoTransaction] = ???

}
