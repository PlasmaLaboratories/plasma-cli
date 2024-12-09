package org.plasmalabs.cli.modules

import cats.effect.IO
import org.plasmalabs.cli.controllers.NodeQueryController
import org.plasmalabs.cli.params.CliParamsParser
import org.plasmalabs.cli.params.models.*
import org.plasmalabs.sdk.dataApi.{NodeQueryAlgebra, RpcChannelResource}
import scopt.OParser

trait NodeQueryModeModule extends RpcChannelResource {

  def nodeQuerySubcmd(
    validateParams: CliParams
  ): IO[Either[String, String]] = {
    val nodeQueryAlgebra = NodeQueryAlgebra.make[IO](
      channelResource(
        validateParams.host,
        validateParams.nodePort,
        validateParams.secureConnection
      )
    )
    validateParams.subcmd match {
      case CliSubCmd.invalid =>
        IO.pure(
          Left(
            OParser.usage(
              CliParamsParser.nodeQueryMode
            ) + "\nA subcommand needs to be specified"
          )
        )
      case CliSubCmd.mintblock =>
        new NodeQueryController(nodeQueryAlgebra)
          .makeBlocks(
            validateParams.nbOfBlocks
          )
      case CliSubCmd.blockbyheight =>
        new NodeQueryController(
          nodeQueryAlgebra
        ).blockByHeight(validateParams.height)
      case CliSubCmd.blockbyid =>
        new NodeQueryController(
          nodeQueryAlgebra
        ).blockById(validateParams.blockId)
      case CliSubCmd.transactionbyid =>
        new NodeQueryController(
          nodeQueryAlgebra
        ).fetchTransaction(validateParams.transactionId)
    }
  }

}
