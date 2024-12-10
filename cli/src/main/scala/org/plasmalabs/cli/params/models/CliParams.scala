package org.plasmalabs.cli.params.models

import org.plasmalabs.sdk.models.{GroupId, LockAddress, SeriesId}

import java.io.File
import scala.collection.immutable.IndexedSeq

final case class CliParams(
  mode:                  CliMode.Value = CliMode.invalid,
  subcmd:                CliSubCmd.Value = CliSubCmd.invalid,
  tokenType:             TokenType.Value = TokenType.all,
  network:               NetworkIdentifiers = NetworkIdentifiers.InvalidNet,
  fromLedger:            LedgerIdentifier.Ledger = LedgerIdentifier.InvalidLedger,
  toLedger:              LedgerIdentifier.Ledger = LedgerIdentifier.InvalidLedger,
  secret:                String = "",
  digestText:            String = "",
  digest:                DigestType = DigestType.InvalidDigest,
  fellowshipName:        String = "self",
  templateName:          String = "default",
  lockTemplate:          String = "",
  inputVks:              Seq[File] = Seq(),
  host:                  String = "",
  nodePort:              Int = 0,
  walletFile:            String = "",
  password:              String = "",
  fromFellowship:        String = "self",
  fromTemplate:          String = "default",
  fromAddress:           Option[String] = None,
  nbOfBlocks:            Int = -1,
  height:                Long = -1,
  blockId:               String = "",
  transactionId:         String = "",
  someFromInteraction:   Option[Int] = None,
  someChangeFellowship:  Option[String] = None,
  someChangeTemplate:    Option[String] = None,
  someChangeInteraction: Option[Int] = None,
  toAddress:             Option[LockAddress] = None,
  someToFellowship:      Option[String] = None,
  someToTemplate:        Option[String] = None,
  amount:                Long = -1,
  fee:                   Long = -1,
  somePassphrase:        Option[String] = None,
  someKeyFile:           Option[String] = None,
  someInputFile:         Option[String] = None,
  someCommitment:        Option[String] = None,
  ephemeralMetadata:     Option[File] = None,
  someOutputFile:        Option[String] = None,
  mnemonic:              Seq[String] = IndexedSeq(),
  someMnemonicFile:      Option[String] = None,
  somePolicyFile:        Option[File] = None,
  someGroupId:           Option[GroupId] = None,
  someSeriesId:          Option[SeriesId] = None,
  secureConnection:      Boolean = false
)
