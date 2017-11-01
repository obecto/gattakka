package com.obecto.gattakka.genetics.operators

import com.obecto.gattakka.{IndividualDescriptor, PipelineOperator}

trait EliteOperator extends PipelineOperator {

  def elitePercentage: Double

  def apply(snapshot: List[IndividualDescriptor]): List[IndividualDescriptor] = {
    val sorted = snapshot sortBy (-_.currentFitness)
    val elites = sorted.slice(0, (snapshot.size * elitePercentage).toInt)
    elites foreach {
      elite =>
        println(s"Elite fitness: ${elite.currentFitness}")
        elite.tempParams.+=("elite" -> true)
    }
    snapshot
  }

}
