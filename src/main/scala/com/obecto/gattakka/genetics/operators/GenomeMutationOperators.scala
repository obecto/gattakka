package com.obecto.gattakka.genetics.operators

import com.obecto.gattakka.genetics.{Genome, Chromosome}
import com.obecto.gattakka.{PipelineOperator}


trait ShuffleMutationOperator extends PipelineOperator with MutationBaseOperator {

  def apply(genome: Genome): Genome = {
    new Genome(rnd.shuffle(genome.chromosomes))
  }

}

trait InsertMutationOperator extends PipelineOperator with MutationBaseOperator {

  def createChromosome(): Chromosome
  def insertionChance: Double

  def apply(genome: Genome): Genome = {
    var newChromosomes = genome.chromosomes flatMap { chromosome =>
      if (rnd.nextFloat() < insertionChance) {
        List(chromosome, createChromosome())
      } else {
        List(chromosome)
      }
    }
    if (rnd.nextFloat() < insertionChance) {
      newChromosomes = createChromosome() +: newChromosomes
    }
    new Genome(newChromosomes)
  }

}

trait DropMutationOperator extends PipelineOperator with MutationBaseOperator {

  def mayDrop(chromosome: Chromosome): Boolean = true
  def dropChance: Double

  def apply(genome: Genome): Genome = {
    new Genome(genome.chromosomes.filterNot { chromosome =>
      rnd.nextFloat() < dropChance && mayDrop(chromosome)
    })
  }

}
