package com.obecto.gattakka.genetics.operators

import com.obecto.gattakka.genetics.Genome
import com.obecto.gattakka.{IndividualDescriptor, PipelineOperator}
import scala.collection.mutable.HashSet

trait DeduplicationOperator extends PipelineOperator {

  def apply(snapshot: List[IndividualDescriptor]): List[IndividualDescriptor] = {
    val genomes = HashSet[Genome]()
    var count = 0
    for (
      individual <- snapshot
      if individual.tempParams.getOrElse("elite", false) == false && !individual.doomedToDie
    ) {
      if (genomes.contains(individual.genome)) {
        individual.doomedToDie = true
        count += 1
      } else {
        genomes += individual.genome
      }
    }
    println(s"Deduplicated ${count} genomes")
    snapshot
  }

}
