package org.plasmalabs.cli.params.mode

import org.plasmalabs.cli.{PlasmaCliMode, PlasmaCliParams, PlasmaCliSubCmd}
import scopt.{OParser, OParserBuilder}

import java.io.File
import java.nio.file.Paths

trait WalletMode extends Args with Coordinates:

  val builder: OParserBuilder[PlasmaCliParams]

  import builder._

  def walletMode: OParser[Unit, PlasmaCliParams] =
    cmd("wallet")
      .action((_, c) => c.copy(mode = PlasmaCliMode.wallet))
      .text("Wallet mode")
      .children(
        cmd("balance")
          .action((_, c) => c.copy(subcmd = PlasmaCliSubCmd.balance))
          .text("Get balance of wallet")
          .children(
            (hostPortNetwork ++ coordinates ++ (Seq(
              fromAddress,
              walletDbArg
            ))): _*
          ),
        cmd("set-interaction")
          .action((_, c) => c.copy(subcmd = PlasmaCliSubCmd.setinteraction))
          .text("Set the current interaction")
          .children(
            coordinates.map(_.required()) ++
            Seq(
              walletDbArg
            ): _*
          ),
        cmd("list-interactions")
          .action((_, c) => c.copy(subcmd = PlasmaCliSubCmd.listinteraction))
          .text("List the interactions for a given fellowship and template")
          .children(
            fellowshipNameArg,
            templateNameArg,
            walletDbArg
          ),
        cmd("sync")
          .action((_, c) => c.copy(subcmd = PlasmaCliSubCmd.sync))
          .text("Sync wallet")
          .children(
            (hostPortNetwork ++ keyfileAndPassword ++ (Seq(
              fellowshipNameArg,
              templateNameArg,
              walletDbArg
            ))): _*
          ),
        cmd("init")
          .action((_, c) => c.copy(subcmd = PlasmaCliSubCmd.init))
          .text("Initialize wallet")
          .children(
            (
              Seq(
                networkArg.required(),
                passwordArg.required(),
                outputArg.optional(),
                newwalletdbArg.required(),
                passphraseArg.optional(),
                opt[Option[String]]("mnemonicfile")
                  .action((x, c) => c.copy(someMnemonicFile = x))
                  .text("Mnemonic output file. (mandatory)")
                  .required()
                  .validate(x =>
                    x.map(f =>
                      if (Paths.get(f).toFile().exists()) {
                        failure("Mnemonic file already exists")
                      } else {
                        success
                      }
                    ).getOrElse(success)
                  )
              )
            ): _*
          ),
        cmd("recover-keys")
          .action((_, c) => c.copy(subcmd = PlasmaCliSubCmd.recoverkeys))
          .text("Recover Wallet Main Key")
          .children(
            (
              Seq(
                networkArg,
                opt[Seq[String]]('m', "mnemonic")
                  .action((x, c) => c.copy(mnemonic = x))
                  .text("Mnemonic for the key. (mandatory)")
                  .validate(x =>
                    if (List(12, 15, 18, 21, 24).contains(x.length)) success
                    else failure("Mnemonic must be 12, 15, 18, 21 or 24 words")
                  ),
                passwordArg,
                outputArg,
                newwalletdbArg,
                passphraseArg
              )
            ): _*
          ),
        cmd("current-address")
          .action((_, c) => c.copy(subcmd = PlasmaCliSubCmd.currentaddress))
          .text("Obtain current address")
          .children(
            (Seq(walletDbArg) ++ coordinates): _*
          ),
        cmd("export-vk")
          .action((_, c) => c.copy(subcmd = PlasmaCliSubCmd.exportvk))
          .text("Export verification key")
          .children(
            (keyfileAndPassword ++ Seq(
              outputArg,
              walletDbArg,
              fellowshipNameArg,
              templateNameArg,
              opt[Option[Int]]("interaction")
                .action((x, c) => c.copy(someFromInteraction = x))
                .text("Interaction from where we are sending the funds from")
            )): _*
          ),
        cmd("add-secret")
          .action((_, c) => c.copy(subcmd = PlasmaCliSubCmd.addsecret))
          .text("Add a secret to the wallet")
          .children(
            walletDbArg,
            secretArg,
            digestArg
          ),
        cmd("get-preimage")
          .action((_, c) => c.copy(subcmd = PlasmaCliSubCmd.getpreimage))
          .text("Get a preimage from the wallet")
          .children(
            walletDbArg,
            digestTextArg,
            digestArg
          ),
        cmd("import-vks")
          .action((_, c) => c.copy(subcmd = PlasmaCliSubCmd.importvks))
          .text("Import verification key")
          .children(
            (keyfileAndPassword ++ Seq(
              walletDbArg,
              fellowshipNameArg,
              templateNameArg,
              opt[Seq[File]]("input-vks")
                .action((x, c) => c.copy(inputVks = x))
                .text("The keys to import. (mandatory)")
            )): _*
          )
      )

end WalletMode
