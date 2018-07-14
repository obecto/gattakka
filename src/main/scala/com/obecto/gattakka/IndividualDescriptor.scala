package com.obecto.gattakka

import com.obecto.gattakka.genetics.Genome
import collection.mutable.{HashMap}

object IndividualState extends Enumeration {
  type IndividualState = Value
  val Normal, DoomedToDie, Elite = Value
}

case class IndividualDescriptor(
  genome: Genome,
  id: Option[String] = None,
  fitness: Double = Double.NaN,
  var tempParams: HashMap[String, Any] = HashMap[String, Any]()
) {
  var state: IndividualState.Value = IndividualState.Normal
}
