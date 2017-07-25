package com.obecto.gattakka.genetics.operators

import com.obecto.gattakka.{IndividualDescriptor, PipelineOperator}

/**
  * Created by gbarn_000 on 7/25/2017.
  */
trait EliteOperator extends PipelineOperator {

  def apply(snapshot: List[IndividualDescriptor]): List[IndividualDescriptor] = {
    val sorted = sortByFitness(snapshot)
    val elites = sorted.slice(0, (snapshot.size * elitePercentage).toInt)
    elites foreach {
      elite =>
        println(s"Elite fitness: ${elite.currentFitness}")
        elite.tempParams.+=("elite" -> true)
    }
    snapshot
  }

  def elitePercentage: Float = 0.2f

  def sortByFitness(snapshot: List[IndividualDescriptor]): List[IndividualDescriptor] = {
    snapshot sortBy {
      -_.currentFitness
    }
  }


}
