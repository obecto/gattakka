package com.obecto.gattakka.genetics.operators

import com.obecto.gattakka.genetics.{Chromosome, Genome}
import com.obecto.gattakka.{IndividualDescriptor, PipelineOperator, Population}

import scala.collection.mutable.ListBuffer
import scala.util.Random

trait BinaryMutationOperator extends MutationBaseOperator with PipelineOperator {

  def rnd: Random = Random

  def apply(snapshot: List[IndividualDescriptor]): List[IndividualDescriptor] = {

    val (onlyElite, withoutElite) = snapshot partition {
      _.tempParams.getOrElse("elite", false) == false
    }

    val withoutDoomedAndElite = withoutElite filter {
      !_.doomedToDie
    }

    val mutatedIndividuals: ListBuffer[IndividualDescriptor] = ListBuffer.empty

    withoutDoomedAndElite foreach {
      individualDescriptor =>
        val genome = individualDescriptor.genome
        var genomeMutationOccured = false

        val newChromosomes = genome.chromosomes map { chromosome =>

          var chromosomeMutationOccured = false
          val mutatedByteArray = chromosome.byteArray map { byte =>
            var newByte = byte
            for (i <- 0 to 8) {
              val randomNum = rnd.nextFloat()
              if (randomNum < mutationChance) {
                chromosomeMutationOccured = true
                newByte = (newByte ^ (1 << i)).toByte
              }
            }
            newByte
          }

          if (chromosomeMutationOccured) {
            genomeMutationOccured = true
            new Chromosome(mutatedByteArray, chromosome.descriptor)
          } else {
            chromosome
          }
        }

        if (genomeMutationOccured) {
          individualDescriptor.doomedToDie = true
          mutatedIndividuals += IndividualDescriptor(Population.getUniqueBotId, new Genome(newChromosomes), None)
        }
    }

    snapshot ++ mutatedIndividuals.toList
  }
}
