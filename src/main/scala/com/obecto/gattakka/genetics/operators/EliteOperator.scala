package com.obecto.gattakka.genetics.operators

import com.obecto.gattakka.{IndividualDescriptor, PipelineOperator}

trait EliteOperator extends PipelineOperator {

  def elitePercentage: Double

  def apply(snapshot: List[IndividualDescriptor]): List[IndividualDescriptor] = {
    val withoutDoomed = snapshot filter (!_.doomedToDie)
    val sorted = withoutDoomed sortBy (-_.currentFitness)
    val elites = sorted.slice(0, (snapshot.size * elitePercentage).ceil.toInt)
    elites foreach {
      elite =>
        println(s"Elite fitness: ${elite.currentFitness}")
        elite.tempParams.+=("elite" -> true)
    }
    snapshot
  }

}
