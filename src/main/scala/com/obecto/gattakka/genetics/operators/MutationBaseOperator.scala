package com.obecto.gattakka.genetics.operators

import com.obecto.gattakka.genetics.{Chromosome, Genome}
import com.obecto.gattakka.{IndividualDescriptor, PipelineOperator, Population}
import scala.collection.mutable.ListBuffer

import scala.util.Random

trait MutationBaseOperator extends PipelineOperator {

  def rnd: Random = Random
  def mutationChance: Double
  def killParent: Boolean = true

  def apply(genome: Genome): Genome

  def apply(snapshot: List[IndividualDescriptor]): List[IndividualDescriptor] = {

    val withoutDoomed = snapshot filterNot (_.doomedToDie)


    val mutatedIndividuals: ListBuffer[IndividualDescriptor] = ListBuffer.empty

    for (desc <- withoutDoomed) {
      if (rnd.nextFloat() < mutationChance) {
        val genome = desc.genome

        val newGenome = apply(genome)

        if (newGenome != genome) {
          if (killParent && desc.tempParams.getOrElse("elite", false) == false) {
            desc.doomedToDie = true
          }
          mutatedIndividuals += IndividualDescriptor(Population.getUniqueBotId, newGenome, None)
        }
      }
    }

    snapshot ++ mutatedIndividuals.toList
  }
}

trait ChromosomeMutationBaseOperator extends PipelineOperator with MutationBaseOperator {

  def apply(chromosome: Chromosome): Chromosome

  def apply(genome: Genome): Genome = {
    var genomeMutationOccured = false

    val newChromosomes = genome.chromosomes map { chromosome =>

      val newChromosome = apply(chromosome)

      if (newChromosome != chromosome) {
        genomeMutationOccured = true
        newChromosome
      } else {
        chromosome
      }
    }

    if (genomeMutationOccured) {
      new Genome(newChromosomes)
    } else {
      genome
    }
  }
}
