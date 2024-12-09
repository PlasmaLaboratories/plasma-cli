package org.plasmalabs.cli.params.models

object CliSubCmd extends Enumeration {
  type CliSubCmd = Value

  val invalid, init, recoverkeys, utxobyaddress, blockbyheight, blockbyid, transactionbyid, create, prove, broadcast,
    currentaddress, list, add, inspect, exportvk, addsecret, getpreimage, importvks, sync, setinteraction,
    listinteraction, balance, mintblock = Value
}
