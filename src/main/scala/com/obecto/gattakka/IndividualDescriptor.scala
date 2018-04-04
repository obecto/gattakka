package com.obecto.gattakka

import com.obecto.gattakka.genetics.Genome

object IndividualState extends Enumeration {
  type IndividualState = Value
  val Normal, DoomedToDie, Elite = Value
}

case class IndividualDescriptor(
  genome: Genome,
  id: Option[String] = None,
  fitness: Double = Double.NaN,
  // var tempParams: immutable.Map[String, Any] = immutable.Map[String, Any]() // YAGNI
) {
  var state: IndividualState.Value = IndividualState.Normal
}
