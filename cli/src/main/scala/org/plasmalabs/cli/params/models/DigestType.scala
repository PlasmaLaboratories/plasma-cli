package org.plasmalabs.cli.params.models

sealed abstract class DigestType(val shortName: String, val digestIdentifier: String)

object DigestType {

  def withName(name: String): DigestType =
    name match {
      case "sha256"  => Sha256
      case "blake2b" => Blake2b
      case _         => InvalidDigest
    }

  case object Sha256 extends DigestType("sha256", "Sha256")

  case object Blake2b extends DigestType("blake2b", "Blake2b256")

  case object InvalidDigest extends DigestType("invalid", "Invalid")
}
