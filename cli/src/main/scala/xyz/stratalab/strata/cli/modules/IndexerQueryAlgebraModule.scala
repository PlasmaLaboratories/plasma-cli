package xyz.stratalab.strata.cli.modules

import xyz.stratalab.sdk.dataApi.{IndexerQueryAlgebra, RpcChannelResource}
import cats.effect.IO

trait IndexerQueryAlgebraModule extends RpcChannelResource {

  def indexerQueryAlgebra(host: String, port: Int, secureConnection: Boolean) =
    IndexerQueryAlgebra.make[IO](
      channelResource(
        host,
        port,
        secureConnection
      )
    )
}
