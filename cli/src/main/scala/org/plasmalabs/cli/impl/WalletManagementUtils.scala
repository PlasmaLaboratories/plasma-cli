package org.plasmalabs.cli.impl

import cats.effect.kernel.Sync
import cats.implicits.*
import org.plasmalabs.crypto.encryption.VaultStore
import org.plasmalabs.quivr.models.KeyPair
import org.plasmalabs.sdk.dataApi.WalletKeyApiAlgebra
import org.plasmalabs.sdk.wallet.WalletApi

class WalletManagementUtils[F[_]: Sync](
  walletApi: WalletApi[F],
  dataApi:   WalletKeyApiAlgebra[F]
) {

  def loadKeys(keyfile: String, password: String): F[KeyPair] =
    for {
      wallet <- readInputFile(keyfile)
      keyPair <-
        walletApi
          .extractMainKey(wallet, password.getBytes())
          .flatMap(
            _.fold(
              _ match {
                case WalletApi.FailedToDecodeWallet(_) =>
                  Sync[F].raiseError[KeyPair](
                    new Throwable(
                      "Failed to decode wallet: check that the password is correct"
                    )
                  )
                case _ =>
                  Sync[F].raiseError[KeyPair] {
                    new Throwable(
                      "There was a problem decoding the wallet (check that the password is correct)"
                    )
                  }
              },
              Sync[F].point(_)
            )
          )
    } yield keyPair

  def readInputFile(
    inputFile: String
  ): F[VaultStore[F]] =
    dataApi
      .getMainKeyVaultStore(inputFile)
      .flatMap(
        _.fold(
          x =>
            Sync[F].raiseError[VaultStore[F]](
              new Throwable("Error reading input file: " + x)
            ),
          Sync[F].point(_)
        )
      )

}
