package xyz.stratalab.strata.cli

import munit.FunSuite

import scopt.OParser

class ParamsNodeQueryTest extends FunSuite {

  import StrataCliParamsParserModule._

  test("Block by height") {
    val args0 = List(
      "node-query",
      "block-by-height",
      "--height",
      "1",
      "--port",
      "9084",
      "-h",
      "localhost"
    )
    assert(OParser.parse(paramParser, args0, StrataCliParams()).isDefined)
  }
  test("Block by id") {
    val args0 = List(
      "node-query",
      "block-by-id",
      "--block-id",
      "8PrjN9RtFK44nmR1dTo1jG2ggaRHaGNYhePEhnWY1TTM",
      "--port",
      "9084",
      "-h",
      "localhost"
    )
    assert(OParser.parse(paramParser, args0, StrataCliParams()).isDefined)
  }
  test("Transaction by id") {
    val args0 = List(
      "node-query",
      "transaction-by-id",
      "--transaction-id",
      "8PrjN9RtFK44nmR1dTo1jG2ggaRHaGNYhePEhnWY1TTM",
      "--port",
      "9084",
      "-h",
      "localhost"
    )
    assert(OParser.parse(paramParser, args0, StrataCliParams()).isDefined)
  }

}
