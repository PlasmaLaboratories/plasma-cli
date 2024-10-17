package org.plasmalabs.strata.cli.modules

import cats.effect.IO
import org.plasmalabs.strata.cli.controllers.NodeQueryController
import org.plasmalabs.strata.cli.StrataCliSubCmd
import org.plasmalabs.sdk.dataApi.{NodeQueryAlgebra, RpcChannelResource}
import org.plasmalabs.strata.cli.StrataCliParams
import scopt.OParser
import org.plasmalabs.strata.cli.StrataCliParamsParserModule

trait NodeQueryModeModule extends RpcChannelResource {

  def nodeQuerySubcmd(
      validateParams: StrataCliParams
  ): IO[Either[String, String]] = {
    val nodeQueryAlgebra = NodeQueryAlgebra.make[IO](
      channelResource(
        validateParams.host,
        validateParams.nodePort,
        validateParams.secureConnection
      )
    )
    validateParams.subcmd match {
      case StrataCliSubCmd.invalid =>
        IO.pure(
          Left(
            OParser.usage(
              StrataCliParamsParserModule.nodeQueryMode
            ) + "\nA subcommand needs to be specified"
          )
        )
      case StrataCliSubCmd.mintblock =>
        new NodeQueryController(nodeQueryAlgebra)
          .makeBlock(
            validateParams.nbOfBlocks
          )
      case StrataCliSubCmd.blockbyheight =>
        new NodeQueryController(
          nodeQueryAlgebra
        ).blockByHeight(validateParams.height)
      case StrataCliSubCmd.blockbyid =>
        new NodeQueryController(
          nodeQueryAlgebra
        ).blockById(validateParams.blockId)
      case StrataCliSubCmd.transactionbyid =>
        new NodeQueryController(
          nodeQueryAlgebra
        ).fetchTransaction(validateParams.transactionId)
    }
  }

}
