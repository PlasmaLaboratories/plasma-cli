package xyz.stratalab.strata.cli.mockbase

import xyz.stratalab.strata.cli.impl.WalletManagementUtils
import cats.effect.kernel.Sync
import xyz.stratalab.crypto.encryption.VaultStore
import quivr.models.KeyPair

class BaseWalletManagementUtils[F[_]: Sync]
    extends WalletManagementUtils[F](null, null) {
  override def loadKeys(keyfile: String, password: String): F[KeyPair] = ???

  override def readInputFile(
      inputFile: String
  ): F[VaultStore[F]] = ???
}
