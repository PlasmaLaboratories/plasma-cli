package org.plasmalabs.cli.modules

import cats.effect.IO
import org.plasmalabs.sdk.dataApi.{IndexerQueryAlgebra, RpcChannelResource}

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
