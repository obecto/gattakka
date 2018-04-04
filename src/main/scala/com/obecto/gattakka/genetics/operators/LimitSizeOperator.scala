package com.obecto.gattakka.genetics.operators

import com.obecto.gattakka.{PipelineOperator, IndividualDescriptor, IndividualState}

trait LimitSizeOperator extends PipelineOperator {

  def targetPopulationSize: Int

  def apply(snapshot: List[IndividualDescriptor]): List[IndividualDescriptor] = {
    // Keeps the size of the population the same
    val withoutDoomed = snapshot filter (_.state != IndividualState.DoomedToDie)
    val withoutDoomedAndNew = withoutDoomed filter (_.id.nonEmpty)
    val toDoom = withoutDoomedAndNew
      .sortBy(x => if (x.fitness == Double.NaN) Double.NegativeInfinity else x.fitness)
      .drop(targetPopulationSize + withoutDoomedAndNew.size - withoutDoomed.size)

    for (individualDescriptor <- toDoom if individualDescriptor.state != IndividualState.Elite) {
      individualDescriptor.state = IndividualState.DoomedToDie
    }

    println(s"Removed ${toDoom.size} genomes");

    snapshot
  }
}
