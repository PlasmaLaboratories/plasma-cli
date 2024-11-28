package org.plasmalabs.cli

import munit.CatsEffectSuite

import java.nio.file.{Files, Path, Paths}

trait CommonFunFixture:

  self: CatsEffectSuite with BaseConstants =>

  val tmpDirectory: FunFixture[Path] = FunFixture[Path](
    setup = { _ =>
      val tmpDir = Paths.get(TMP_DIR).toFile
      if (tmpDir.exists()) {
        Paths.get(TMP_DIR).toFile.listFiles().map(_.delete()).mkString("\n")
        Files.deleteIfExists(Paths.get(TMP_DIR))
      }
      Files.createDirectory(Paths.get("./tmp"))
    },
    teardown = { _ => () }
  )

end CommonFunFixture
