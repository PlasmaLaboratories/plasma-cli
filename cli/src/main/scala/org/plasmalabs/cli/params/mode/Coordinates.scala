package org.plasmalabs.cli.params.mode

import org.plasmalabs.cli.{PlasmaCliMode, PlasmaCliParams, PlasmaCliSubCmd}
import scopt.{OParser, OParserBuilder}

trait Coordinates extends Args:

  val builder: OParserBuilder[PlasmaCliParams]

  import builder._

  def coordinates: Seq[OParser[? >: String & Option[Int] <: Serializable, PlasmaCliParams]] =
    Seq(
      opt[String]("from-fellowship")
        .action((x, c) => c.copy(fromFellowship = x))
        .text("Fellowship where we are sending the funds from")
        .optional(),
      opt[String]("from-template")
        .action((x, c) => c.copy(fromTemplate = x))
        .text("Template where we are sending the funds from")
        .optional(),
      opt[Option[Int]]("from-interaction")
        .action((x, c) => c.copy(someFromInteraction = x))
        .validate(
          _.map(x =>
            if (x >= 1) success
            else failure("Interaction needs to be greater or equal to 1")
          ).getOrElse(success)
        )
        .text("Interaction from where we are sending the funds from")
    )

  def changeCoordinates: Seq[OParser[? >: Option[String] & Option[Int] & Unit, PlasmaCliParams]] =
    Seq(
      opt[Option[String]]("change-fellowship")
        .action((x, c) => c.copy(someChangeFellowship = x))
        .text("Fellowship where we are sending the change to")
        .optional(),
      opt[Option[String]]("change-template")
        .action((x, c) => c.copy(someChangeTemplate = x))
        .text("Template where we are sending the change to")
        .optional(),
      opt[Option[Int]]("change-interaction")
        .action((x, c) => c.copy(someChangeInteraction = x))
        .text("Interaction where we are sending the change to")
        .optional(),
      checkConfig(c =>
        if (c.mode == PlasmaCliMode.simpletransaction && c.subcmd == PlasmaCliSubCmd.create) {
          if (c.fromFellowship == "nofellowship") {
            (
              c.someChangeFellowship,
              c.someChangeTemplate,
              c.someChangeInteraction
            ) match {
              case (Some(_), Some(_), Some(_)) =>
                success
              case (_, _, _) =>
                failure(
                  "You must specify a change-fellowship, change-template and change-interaction when using nofellowship"
                )
            }
          } else {
            (
              c.someChangeFellowship,
              c.someChangeTemplate,
              c.someChangeInteraction
            ) match {
              case (Some(_), Some(_), Some(_)) =>
                success
              case (None, None, None) =>
                success
              case (_, _, _) =>
                failure(
                  "You must specify a change-fellowship, change-template and change-interaction or not specify any of them"
                )
            }
          }
        } else // if you need to set the change you set all the parameters
          (
            c.someChangeFellowship,
            c.someChangeTemplate,
            c.someChangeInteraction
          ) match {
            case (Some(_), Some(_), Some(_)) =>
              success
            case (None, None, None) =>
              success
            case (_, _, _) =>
              failure(
                "You must specify a change-fellowship, change-template and change-interaction or not specify any of them"
              )
          }
      )
    )

end Coordinates
