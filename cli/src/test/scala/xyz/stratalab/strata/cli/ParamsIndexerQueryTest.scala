package xyz.stratalab.strata.cli

import munit.FunSuite
import scopt.OParser

class ParamsIndexerQueryTest extends FunSuite {

  import StrataCliParamsParserModule._

  test("Test from-fellowship transactions require index (UTXO query)") {
    val args0 = List(
      "indexer-query",
      "utxo-by-address",
      "--from-fellowship",
      "nofellowship",
      "--from-template",
      "genesis",
      "--from-interaction",
      "1",
      "--port",
      "9084",
      "-h",
      "localhost",
      "--walletdb",
      "src/test/resources/wallet.db"
    )
    assert(OParser.parse(paramParser, args0, StrataCliParams()).isDefined)
  }

}
