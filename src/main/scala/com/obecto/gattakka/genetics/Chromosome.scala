package com.obecto.gattakka.genetics

class Chromosome(var genes: Seq[Gene[_]] = Array[Gene[_]]()) {
  var calculatedFitness: Float = Float.NaN

  def withFitness(newFitness: Float): Chromosome = {
    val newChromosome = new Chromosome(genes)
    newChromosome.calculatedFitness = newFitness

    newChromosome
  }
}
