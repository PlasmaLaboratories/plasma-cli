package org.plasmalabs.cli.modules

import cats.effect.IO
import org.plasmalabs.cli.controllers.NodeQueryController
import org.plasmalabs.cli.PlasmaCliSubCmd
import org.plasmalabs.sdk.dataApi.{NodeQueryAlgebra, RpcChannelResource}
import org.plasmalabs.cli.PlasmaCliParams
import scopt.OParser
import org.plasmalabs.cli.PlasmaCliParamsParserModule

trait NodeQueryModeModule extends RpcChannelResource {

  def nodeQuerySubcmd(
    validateParams: PlasmaCliParams
  ): IO[Either[String, String]] = {
    val nodeQueryAlgebra = NodeQueryAlgebra.make[IO](
      channelResource(
        validateParams.host,
        validateParams.nodePort,
        validateParams.secureConnection
      )
    )
    validateParams.subcmd match {
      case PlasmaCliSubCmd.invalid =>
        IO.pure(
          Left(
            OParser.usage(
              PlasmaCliParamsParserModule.nodeQueryMode
            ) + "\nA subcommand needs to be specified"
          )
        )
      case PlasmaCliSubCmd.mintblock =>
        new NodeQueryController(nodeQueryAlgebra)
          .makeBlocks(
            validateParams.nbOfBlocks
          )
      case PlasmaCliSubCmd.blockbyheight =>
        new NodeQueryController(
          nodeQueryAlgebra
        ).blockByHeight(validateParams.height)
      case PlasmaCliSubCmd.blockbyid =>
        new NodeQueryController(
          nodeQueryAlgebra
        ).blockById(validateParams.blockId)
      case PlasmaCliSubCmd.transactionbyid =>
        new NodeQueryController(
          nodeQueryAlgebra
        ).fetchTransaction(validateParams.transactionId)
    }
  }

}
