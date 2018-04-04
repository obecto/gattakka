package com.obecto.gattakka.genetics.operators

import com.obecto.gattakka.{IndividualDescriptor, IndividualState, PipelineOperator}
import scala.collection.mutable.HashSet

trait DeduplicationOperator extends PipelineOperator {

  def apply(snapshot: List[IndividualDescriptor]): List[IndividualDescriptor] = {
    // val genomes = HashSet[List[String]]()
    val genomes = HashSet[List[Vector[Byte]]]()
    var count = 0
    for (
      individual <- snapshot
      if individual.state == IndividualState.Normal
    ) {
      val hashableGenome = individual.genome.chromosomes.map({ chromosome =>
        // new String(chromosome.byteArray)
        chromosome.byteArray.toVector
      })
      if (genomes(hashableGenome)) {
        individual.state = IndividualState.DoomedToDie
        count += 1
      } else {
        genomes += hashableGenome
      }
    }
    println(s"Deduplicated ${count} genomes")
    snapshot
  }

}
