package org.plasmalabs.plasma.cli

case class WalletKeyConfig(
    walletFile: String,
    keyFile: String,
    password: String,
    mnemonicFile: String
)

trait BaseConstants {

  val TMP_DIR = "./tmp"

  val BASE_AMOUNT = 10000

  val BASE_FEE = 10

  val HOST = "localhost" // "testnet.topl.co"

  val NODE_PORT = 9084 // 443

}
