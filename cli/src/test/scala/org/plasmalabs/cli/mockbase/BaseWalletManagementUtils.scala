package org.plasmalabs.cli.mockbase

import org.plasmalabs.cli.impl.WalletManagementUtils
import cats.effect.kernel.Sync
import org.plasmalabs.crypto.encryption.VaultStore
import quivr.models.KeyPair

class BaseWalletManagementUtils[F[_]: Sync]
    extends WalletManagementUtils[F](null, null) {
  override def loadKeys(keyfile: String, password: String): F[KeyPair] = ???

  override def readInputFile(
      inputFile: String
  ): F[VaultStore[F]] = ???
}
