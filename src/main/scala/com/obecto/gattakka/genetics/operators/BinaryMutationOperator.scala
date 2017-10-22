package com.obecto.gattakka.genetics.operators

import com.obecto.gattakka.genetics.{Chromosome, Genome}
import com.obecto.gattakka.{IndividualDescriptor, PipelineOperator, Population}

import scala.collection.mutable.ListBuffer

/**
  * Created by gbarn_000 on 7/25/2017.
  */

trait BinaryMutationOperator extends MutationBaseOperator with PipelineOperator {

  def apply(snapshot: List[IndividualDescriptor]): List[IndividualDescriptor] = {

    val withoutElitesAndDoomed = snapshot filter {
      individualDescriptor =>
        var isElite = false
        individualDescriptor.tempParams.get("elite") match {
          case Some(elite) => isElite = elite.asInstanceOf[Boolean]
          case None =>
        }
        !(isElite || individualDescriptor.doomedToDie)
    }

    val mutatedIndividuals: ListBuffer[IndividualDescriptor] = ListBuffer.empty

    val rng: scala.util.Random = scala.util.Random

    withoutElitesAndDoomed foreach {
      individualDescriptor =>
        val genome = individualDescriptor.genome
        var mutationOccured = false

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
          mutatedIndividuals += IndividualDescriptor(Population.getUniqueBotId, new Genome(newChromosomes), None)
        }
    }

    snapshot ++ mutatedIndividuals.toList
  }
}
