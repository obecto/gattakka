package com.obecto.gattakka.genetics.operators

import com.obecto.gattakka.genetics.{Chromosome, Genome}
import com.obecto.gattakka.{IndividualDescriptor, PipelineOperator}

import scala.collection.mutable.ListBuffer

/**
  * Created by gbarn_000 on 7/25/2017.
  */

trait BinaryMutationOperator extends MutationBaseOperator with PipelineOperator {

  def apply(snapshot: List[IndividualDescriptor]): List[IndividualDescriptor] = {

    val withoutElites = snapshot filter {
      individualDescriptor =>
        var isElite = false
        individualDescriptor.tempParams.get("elite") match {
          case Some(elite) => isElite = elite.asInstanceOf[Boolean]
          case None =>
        }
        !isElite
    }

    val mutatedIndividuals: ListBuffer[IndividualDescriptor] = ListBuffer.empty

    val rng: scala.util.Random = scala.util.Random

    withoutElites foreach {
      individualDescriptor =>

        val genome = individualDescriptor.genome
        var mutationOccured = false
        var mutationOccuredNum = 0

        val newChromosomes = genome.chromosomes map {
          chromosome =>
            val chromosomeByteArray = chromosome.toByteArray
            val mutatedByteArray = chromosomeByteArray map {
              byte =>
                var newByte = byte
                for (i <- 0 to 8) {
                  val randomNum = rng.nextFloat()
                  if (randomNum < mutationChance) {
                    mutationOccured = true
                    newByte = (newByte ^ (1 << i)).toByte
                  }
                }
                newByte
            }
            new Chromosome(mutatedByteArray)
        }

        if (mutationOccured) {
          individualDescriptor.doomedToDie = true
          mutationOccuredNum += 1
          mutatedIndividuals += IndividualDescriptor(new Genome(newChromosomes), None)
        }
    }

    snapshot ++ mutatedIndividuals.toList
  }
}
