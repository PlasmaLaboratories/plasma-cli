package org.plasmalabs.cli.views

import org.plasmalabs.sdk.display.DisplayOps.DisplayTOps
import org.plasmalabs.sdk.models.transaction.IoTransaction
import org.plasmalabs.consensus.models.BlockId

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
