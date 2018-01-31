package com.obecto.gattakka.genetics.operators

import com.obecto.gattakka.{PipelineOperator, IndividualDescriptor}

trait LimitSizeOperator extends PipelineOperator {

  def targetPopulationSize: Int

  def apply(snapshot: List[IndividualDescriptor]): List[IndividualDescriptor] = {
    // Keeps the size of the population the same
    val withoutDoomed = snapshot filterNot (_.doomedToDie)
    val withoutDoomedAndNew = withoutDoomed filterNot (_.currentFitness.isNaN())
    val toDoom = withoutDoomedAndNew
      .sorted(Ordering[IndividualDescriptor].reverse)
      .drop(targetPopulationSize + withoutDoomedAndNew.size - withoutDoomed.size)

    for (individualDescriptor <- toDoom if individualDescriptor.tempParams.getOrElse("elite", false) == false) {
      individualDescriptor.doomedToDie = true
    }

    println(s"Removed ${toDoom.size} genomes");

    snapshot
  }
}
