package org.plasmalabs.cli

import cats.effect.ExitCode
import munit.CatsEffectSuite

import scala.concurrent.duration.Duration

class MintingTests
    extends CatsEffectSuite
    with CommonFunctions
    with MintingFunctions
    with CommonTxOperations
    with AliceConstants
    with BobConstants
    with PolicyTemplates
    with CommonFunFixture {

  override val munitIOTimeout: Duration = Duration(180, "s")

  val secure = false

  tmpDirectory.test("Move funds from genesis to alice") { _ =>
    assertIO(
      moveFundsFromGenesisToAlice(secure),
      ExitCode.Success
    )
  }

  test("Use alice's funds to mint a group") {
    assertIO(
      mintGroup(secure),
      ExitCode.Success
    )
  }

  test("Use alice's funds to mint a series") {
    assertIO(
      mintSeries(secure),
      ExitCode.Success
    )
  }

  test("Use alice's funds to mint an asset") {
    assertIO(
      mintAsset(secure),
      ExitCode.Success
    )
  }

}
