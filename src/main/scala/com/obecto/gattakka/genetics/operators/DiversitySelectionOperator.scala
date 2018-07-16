package com.obecto.gattakka.genetics.operators

import com.obecto.gattakka.{IndividualDescriptor, IndividualState, PipelineOperator}

trait DiversitySelectionOperator extends PipelineOperator {

  def targetPopulationSize: Int

  def apply(snapshot: List[IndividualDescriptor]): List[IndividualDescriptor] = {
    val elites = snapshot.filter(_.state == IndividualState.Elite)
    val nonElites = snapshot.filter(_.state == IndividualState.Normal)

    if ((elites.size + nonElites.size) > targetPopulationSize) {
      for (individual <- nonElites) {
        var diversity: Double = 0
        for (elite <- elites) {
          diversity += individual.genome.diversity(elite.genome)
        }

        for (nonElite <- nonElites if nonElite != individual) {
          diversity += individual.genome.diversity(nonElite.genome)
        }

        individual.tempParams("diversity") = diversity / (elites.size + nonElites.size)
      }

      val sizeToDoom = nonElites.size + elites.size - targetPopulationSize
      nonElites.sortBy(_.tempParams("diversity").asInstanceOf[Double].doubleValue())
        .take(sizeToDoom).map(_.state = IndividualState.DoomedToDie)
    }

    val elitesCount = snapshot.filter(_.state == IndividualState.Elite).size
    val nonElitesCount = snapshot.filter(_.state == IndividualState.Normal).size
    val toDoomCount = snapshot.filter(_.state == IndividualState.DoomedToDie).size
    println("Diversity Selection")
    println(s"elites: $elitesCount | nonElites: $nonElitesCount | doomedToDie: $toDoomCount")

    snapshot
  }

}
