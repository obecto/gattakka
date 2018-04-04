package com.obecto.gattakka.genetics.operators

import com.obecto.gattakka.genetics.{Chromosome, Genome}
import com.obecto.gattakka.{IndividualDescriptor,IndividualState, PipelineOperator}
import scala.collection.mutable.ListBuffer

import scala.util.Random

trait MutationBaseOperator extends PipelineOperator {

  def rnd: Random = Random
  def mutationChance: Double
  def killParent: Boolean = true

  def apply(genome: Genome): Genome

  def apply(snapshot: List[IndividualDescriptor]): List[IndividualDescriptor] = {

    val withoutDoomed = snapshot filter (_.state != IndividualState.DoomedToDie)


    val mutatedIndividuals: ListBuffer[IndividualDescriptor] = ListBuffer.empty

    for (descriptor <- withoutDoomed) {
      if (rnd.nextFloat() < mutationChance) {
        val genome = descriptor.genome

        val newGenome = apply(genome)

        if (newGenome != genome) {
          if (killParent && descriptor.state != IndividualState.Elite) {
            descriptor.state = IndividualState.DoomedToDie
          }
          mutatedIndividuals += IndividualDescriptor(newGenome)
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
