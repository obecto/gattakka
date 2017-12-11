package com.obecto.gattakka.genetics.operators

import com.obecto.gattakka.IndividualDescriptor

class RouletteWheelParentSelectionStrategy(rng: scala.util.Random = scala.util.Random) extends ParentSelectionStrategy {

  def select(from: Seq[IndividualDescriptor]): IndividualDescriptor = {
    val totalFitness = calculateTotalFitness(from)
    val randomLimit = totalFitness * rng.nextFloat()
    var reached = 0.0

    from find {
      desc =>
        reached += desc.currentFitness
        reached >= randomLimit
    } match {
      case Some(value) => value
      case None => throw new ArithmeticException("Unable to select parent due to problem in fitness calculation.")
    }
  }

  private def calculateTotalFitness(individualDescriptors: Seq[IndividualDescriptor]): Double = {
    var totalFitness = 0.0
    individualDescriptors.foreach(totalFitness += _.currentFitness)
    totalFitness
  }

}
