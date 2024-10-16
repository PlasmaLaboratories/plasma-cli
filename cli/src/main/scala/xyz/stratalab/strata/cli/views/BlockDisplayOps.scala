package xyz.stratalab.strata.cli.views

import xyz.stratalab.sdk.display.DisplayOps.DisplayTOps
import xyz.stratalab.sdk.models.transaction.IoTransaction
import xyz.stratalab.consensus.models.BlockId

object BlockDisplayOps {

  def display(
      blockId: BlockId,
      ioTransactions: Seq[IoTransaction]
  ): String =
    s"""
BlockId: ${blockId.display}

Block Body:
${ioTransactions.map(_.display).mkString("\n------------\n")}
"""

}
