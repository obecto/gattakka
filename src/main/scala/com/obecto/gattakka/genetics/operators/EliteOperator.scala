package com.obecto.gattakka.genetics.operators

import com.obecto.gattakka.{IndividualDescriptor, PipelineOperator, IndividualState}

trait EliteOperator extends PipelineOperator {

  def elitePercentage: Double

  def apply(snapshot: List[IndividualDescriptor]): List[IndividualDescriptor] = {
    val withoutDoomed = snapshot filter (_.state != IndividualState.DoomedToDie)
    val sorted = withoutDoomed sortBy (-_.fitness)
    val elites = sorted.slice(0, (snapshot.size * elitePercentage).ceil.toInt)
    for (descriptor <- elites) {
      println(s"Elite fitness: ${descriptor.fitness}")
      descriptor.state = IndividualState.Elite
    }
    snapshot
  }

}
