package org.plasmalabs.cli.modules

import cats.effect.IO
import org.plasmalabs.cli.parsers.{AssetMintingStatementParser, GroupPolicyParser, SeriesPolicyParser}

object ParserModule:

  trait Series:

    def seriesPolicyParser(networkId: Int): SeriesPolicyParser[IO] =
      SeriesPolicyParser.make[IO](networkId)

  end Series

  trait Group:

    def groupPolicyParser(networkId: Int): GroupPolicyParser[IO] =
      GroupPolicyParser.make[IO](networkId)

  end Group

  trait Ams:

    def assetMintingStatementParser(networkId: Int): AssetMintingStatementParser[IO] =
      AssetMintingStatementParser.make[IO](networkId)

  end Ams

end ParserModule
