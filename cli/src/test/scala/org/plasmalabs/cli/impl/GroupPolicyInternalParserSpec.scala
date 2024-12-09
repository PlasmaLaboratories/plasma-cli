package org.plasmalabs.cli.impl

import cats.effect.IO
import cats.effect.kernel.Resource
import munit.CatsEffectSuite
import org.plasmalabs.cli.modules.ParserModule
import org.plasmalabs.cli.parsers.InvalidHex
import org.plasmalabs.sdk.constants.NetworkConstants

class GroupPolicyInternalParserSpec extends CatsEffectSuite with ParserModule.Group {

  test(
    "parseGroupPolicy should support transactions with no fixed series"
  ) {
    val parser = groupPolicyParser(NetworkConstants.PRIVATE_NETWORK_ID)
    assertIO(
      parser
        .parseGroupPolicy(
          Resource.make(
            IO.delay(
              scala.io.Source.fromFile(
                "src/test/resources/valid_group_policy.yaml"
              )
            )
          )(source => IO.delay(source.close()))
        )
        .map(policy => policy.toOption.get.fixedSeries),
      None
    )
  }

  test(
    "parseGroupPolicy should support transactions with fixed series"
  ) {
    val parser = groupPolicyParser(NetworkConstants.PRIVATE_NETWORK_ID)
    assertIO(
      parser
        .parseGroupPolicy(
          Resource.make(
            IO.delay(
              scala.io.Source.fromFile(
                "src/test/resources/valid_group_policy_fixed_series.yaml"
              )
            )
          )(source => IO.delay(source.close()))
        )
        .map(policy => policy.toOption.isDefined),
      true
    )
  }

  test(
    "parseGroupPolicy should fail if fixed seriesl too large"
  ) {
    val parser = groupPolicyParser(NetworkConstants.PRIVATE_NETWORK_ID)
    assertIO(
      parser
        .parseGroupPolicy(
          Resource.make(
            IO.delay(
              scala.io.Source.fromFile(
                "src/test/resources/invalid_group_policy_fixed_series.yaml"
              )
            )
          )(source => IO.delay(source.close()))
        )
        .map { policy =>
          policy
        },
      Left(
        InvalidHex(
          "The hex string for the series must be 32 bytes long"
        )
      )
    )
  }

}
