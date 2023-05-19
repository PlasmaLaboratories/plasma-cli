package co.topl.brambl.cli.impl

import cats.arrow.FunctionK
import cats.data.Kleisli
import cats.effect.kernel.Resource
import cats.effect.kernel.Sync
import co.topl.node.services.FetchBlockBodyReq
import co.topl.node.services.FetchBlockIdAtHeightReq
import co.topl.node.services.FetchTransactionReq
import co.topl.node.services.NodeRpcGrpc
import io.grpc.ManagedChannel

trait BifrostQueryInterpreter {

  def interpretADT[A, F[_]: Sync](
      channelResource: Resource[F, ManagedChannel],
      computation: BifrostQueryAlgebra.BifrostQueryADTMonad[A]
  ) = {
    type ChannelContextKlesli[A] =
      Kleisli[F, NodeRpcGrpc.NodeRpcBlockingStub, A]
    val kleisliComputation = computation.foldMap[ChannelContextKlesli](
      new FunctionK[BifrostQueryAlgebra.BifrostQueryADT, ChannelContextKlesli] {

        override def apply[A](
            fa: BifrostQueryAlgebra.BifrostQueryADT[A]
        ): ChannelContextKlesli[A] = {
          import cats.implicits._
          fa match {
            case BifrostQueryAlgebra.FetchBlockBody(blockId) =>
              Kleisli(blockingStub =>
                Sync[F]
                  .blocking(
                    blockingStub
                      .fetchBlockBody(
                        FetchBlockBodyReq(blockId)
                      )
                  )
                  .map(_.body.asInstanceOf[A])
              )
            case BifrostQueryAlgebra.FetchTransaction(txId) =>
              Kleisli(blockingStub =>
                Sync[F]
                  .blocking(
                    blockingStub
                      .fetchTransaction(
                        FetchTransactionReq(txId)
                      )
                  )
                  .map(_.transaction.asInstanceOf[A])
              )
            case BifrostQueryAlgebra.BlockByHeight(height) =>
              Kleisli(blockingStub =>
                Sync[F]
                  .blocking(
                    blockingStub
                      .fetchBlockIdAtHeight(
                        FetchBlockIdAtHeightReq(height)
                      )
                  )
                  .map(_.blockId.asInstanceOf[A])
              )
          }
        }
      }
    )
    (for {
      channel <- channelResource
    } yield channel).use { channel =>
      kleisliComputation.run(NodeRpcGrpc.blockingStub(channel))
    }
  }

}
