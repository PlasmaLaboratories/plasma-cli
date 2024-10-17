package org.plasmalabs.strata.cli.impl

import cats.effect.IO

trait GroupPolicyParserModule {

  def groupPolicyParserAlgebra(networkId: Int) =
    GroupPolicyParser.make[IO](networkId)

}
