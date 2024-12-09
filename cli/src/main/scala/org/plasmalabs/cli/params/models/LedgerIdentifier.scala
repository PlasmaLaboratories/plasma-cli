package org.plasmalabs.cli.params.models

import org.plasmalabs.sdk.constants.NetworkConstants

/**
 * MAIN_LEDGER_ID    = 0xe7b07a00  = 3887102464 = -407864832 (Decimal from signed 2's complement 9 digits)
 *
 * ACCOUNT_LEDGER_ID = 0xe7b07a01  = 3887102465 = -407864831 (Decimal from signed 2's complement 9 digits)
 *
 * @param id id
 */

object LedgerIdentifier:

  sealed abstract class Ledger(id: Int):

    def fromString(s: String): Ledger =
      s match {
        case "main"    => MainLedger
        case "account" => AccountLedger
        case _         => InvalidLedger
      }

  end Ledger

  case object MainLedger extends Ledger(id = NetworkConstants.MAIN_LEDGER_ID)
  case object AccountLedger extends Ledger(id = NetworkConstants.ACCOUNT_LEDGER_ID)
  case object InvalidLedger extends Ledger(id = -1)

end LedgerIdentifier
