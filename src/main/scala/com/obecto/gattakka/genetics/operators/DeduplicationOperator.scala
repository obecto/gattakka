package com.obecto.gattakka.genetics.operators

import com.obecto.gattakka.{IndividualDescriptor, PipelineOperator}
import scala.collection.mutable.HashSet

trait DeduplicationOperator extends PipelineOperator {

  def apply(snapshot: List[IndividualDescriptor]): List[IndividualDescriptor] = {
    // val genomes = HashSet[List[String]]()
    val genomes = HashSet[List[Vector[Byte]]]()
    var count = 0
    for (
      individual <- snapshot
      if individual.tempParams.getOrElse("elite", false) == false && !individual.doomedToDie
    ) {
      val hashableGenome = individual.genome.chromosomes.map({ chromosome =>
        // new String(chromosome.byteArray)
        chromosome.byteArray.toVector
      })
      if (genomes(hashableGenome)) {
        individual.doomedToDie = true
        count += 1
      } else {
        genomes += hashableGenome
      }
    }
    println(s"Deduplicated ${count} genomes")
    snapshot
  }

}
