package com.obecto.gattakka.genetics

class VariableMutationChromosome(
    mutationGene: Gene[_],
    additionGenes: Seq[Gene[_]] = Array[Gene[_]]()
  ) extends Chromosome(additionGenes :+ mutationGene) with ChromosomeBase[VariableMutationChromosome] {

  override def getMutationRate(): Float = {
    return mutationGene.toFloat * 0.7f + 0.3f
  }

  override def withFitness(newFitness: Float): VariableMutationChromosome = {
    val newChromosome = new VariableMutationChromosome(mutationGene, genes.slice(0, genes.size - 1))
    newChromosome.calculatedFitness = newFitness

    newChromosome
  }

  override def withGenes(newGenes: Seq[Gene[_]]): VariableMutationChromosome = {
    assert(newGenes.size == genes.size)
    new VariableMutationChromosome(newGenes(newGenes.size - 1), newGenes.slice(0, newGenes.size - 1))
  }
}
