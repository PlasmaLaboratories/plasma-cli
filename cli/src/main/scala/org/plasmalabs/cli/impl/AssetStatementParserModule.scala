package org.plasmalabs.cli.impl

import cats.effect.IO

trait AssetStatementParserModule {
  def assetMintingStatementParserAlgebra(networkId: Int) =
    AssetMintingStatementParser.make[IO](networkId)

}
