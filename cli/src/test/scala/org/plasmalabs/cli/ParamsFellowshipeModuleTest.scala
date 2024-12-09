package org.plasmalabs.cli

import munit.FunSuite
import org.plasmalabs.cli.params.models.CliParams
import scopt.OParser

import java.io.File
import java.nio.file.{Files, Path, Paths}

class ParamsFellowshipModuleTest extends FunSuite {

  import org.plasmalabs.cli.params.CliParamsParser._

  val tmpWallet = FunFixture[Path](
    setup = { _ =>
      val file = new File("wallet.db")
      file.createNewFile();
      Paths.get(file.getAbsolutePath().toString())
    },
    teardown = { initialWalletDb =>
      if (Files.exists(initialWalletDb))
        Files.delete(initialWalletDb)
    }
  )

  tmpWallet.test("Test fellowship add (happy path)") { _ =>
    val args0 = List(
      "fellowships",
      "add",
      "--walletdb",
      "wallet.db",
      "--fellowship-name",
      "test"
    )
    assert(
      OParser
        .parse(paramParser, args0, CliParams())
        .isDefined
    )
  }

  tmpWallet.test("Test fellowships add invalid(TSDK-760)") { _ =>
    val args0 = List(
      "fellowships",
      "add",
      "--walletdb",
      "wallet.db"
    )
    assert(
      OParser
        .parse(paramParser, args0, CliParams())
        .isEmpty
    )
  }

}
