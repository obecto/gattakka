package com.obecto.gattakka.genetics

case class Chromosome(var genes: Seq[Gene[_]] = Array[Gene[_]]()) extends ChromosomeBase[Chromosome] {
  override def getMutationRate(): Float = {
    return 1f
  }

  override def withFitness(newFitness: Float): Chromosome = {
    val newChromosome = new Chromosome(genes)
    newChromosome.calculatedFitness = newFitness

    newChromosome
  }

  override def withGenes(newGenes: Seq[Gene[_]]): Chromosome = {
    assert(newGenes.size == genes.size)
    new Chromosome(newGenes)
  }
}
