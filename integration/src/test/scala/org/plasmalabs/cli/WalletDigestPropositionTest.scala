package org.plasmalabs.cli

import cats.effect.ExitCode
import munit.CatsEffectSuite

import java.nio.file.{Files, Path, Paths}

class WalletDigestPropositionTest extends CatsEffectSuite with WalletConstants with CommonTxOperations {

  val tmpDirectory = FunFixture[Path](
    setup = { _ =>
      val tmpDir = Paths.get(TMP_DIR).toFile()
      if (tmpDir.exists()) {
        Paths.get(TMP_DIR).toFile().listFiles().map(_.delete()).mkString("\n")
        Files.deleteIfExists(Paths.get(TMP_DIR))
      }
      Files.createDirectory(Paths.get(TMP_DIR))
      Files.createFile(Paths.get(EMPTY_FILE))
    },
    teardown = { _ => () }
  )

  tmpDirectory.test("Initialize wallet and add digest (sha256) template") { _ =>
    for {
      _ <- createWallet().run(walletContext)
      _ <- assertIO(
        addTemplate(
          "sha256Template",
          "threshold(1, sha256(b39f7e1305cd9107ed9af824fcb0729ce9888bbb7f219cc0b6731332105675dc))"
        ).run(walletContext),
        ExitCode.Success
      )
      _ <- importVk("nofellowship", "sha256Template", EMPTY_FILE).run(
        walletContext
      )
      _ <- assertIO(
        walletController(WALLET)
          .currentaddress("nofellowship", "sha256Template", None),
        Some("ptetP7jshHUxpB3Aep7erqzXRtSpcYnpxwcyCVQCpktktNhrUeSWgQH1hmvP")
      )
    } yield ()
  }

  tmpDirectory.test("Initialize wallet and add digest (blake2b) template") { _ =>
    for {
      _ <- createWallet().run(walletContext)
      _ <- assertIO(
        addTemplate(
          "blake2bTemplate",
          "threshold(1, blake2b(b39f7e1305cd9107ed9af824fcb0729ce9888bbb7f219cc0b6731332105675dc))"
        ).run(walletContext),
        ExitCode.Success
      )
      _ <- importVk("nofellowship", "blake2bTemplate", EMPTY_FILE).run(
        walletContext
      )
      _ <- assertIO(
        walletController(WALLET)
          .currentaddress("nofellowship", "blake2bTemplate", None),
        Some("ptetP7jshHUzFqDR9cjYFnRt5caJAYmUVToDkmjSzvXUjVMFZDtDoEc7tRfT")
      )
    } yield ()
  }

  tmpDirectory.test("Initialize wallet and add secret (sha256)") { _ =>
    for {
      _ <- createWallet().run(walletContext)
      _ <- assertIO(
        addSecret(
          "topl-secret",
          "sha256"
        ).run(walletContext),
        ExitCode.Success
      )
      _ <- assertIO(
        getPreimage(
          "ee15b31e49931db6551ed8a82f1422ce5a5a8debabe8e81a724c88f79996d0df",
          "sha256"
        ).run(walletContext),
        ExitCode.Success
      )
      _ <- assertIO(
        walletController(WALLET)
          .getPreimage(
            Sha256,
            "ee15b31e49931db6551ed8a82f1422ce5a5a8debabe8e81a724c88f79996d0df"
          )
          .map(_.toOption),
        Some("Preimage: topl-secret")
      )
    } yield ()
  }

  tmpDirectory.test("Initialize wallet and add secret (blake2b)") { _ =>
    for {
      _ <- createWallet().run(walletContext)
      _ <- assertIO(
        addSecret(
          "topl-secret",
          "blake2b"
        ).run(walletContext),
        ExitCode.Success
      )
      _ <- assertIO(
        getPreimage(
          "0a0f4e1461688b3dbf01cad2882e5779998efcf7ee3800c80e964fd0424d7e0c",
          "blake2b"
        ).run(walletContext),
        ExitCode.Success
      )
      _ <- assertIO(
        walletController(WALLET)
          .getPreimage(
            Blake2b,
            "0a0f4e1461688b3dbf01cad2882e5779998efcf7ee3800c80e964fd0424d7e0c"
          )
          .map(_.toOption),
        Some("Preimage: topl-secret")
      )
    } yield ()
  }
}
