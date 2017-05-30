package com.obecto.genetics

class Population(var chromosomes: Array[Chromosome] = Array[Chromosome]()) {

  var totalFitness: Double = 0.0
  var minFitness: Float = Float.MaxValue
  var maxFitness: Float = Float.MinValue
  if (chromosomes.size != 0) {
    recomputeValues()
  }

  def recomputeValues(): Unit = {
    totalFitness = 0.0
    minFitness = Float.MaxValue
    maxFitness = Float.MinValue
    for(chromosome <- chromosomes) {
      totalFitness += chromosome.calculatedFitness
      minFitness = Math.min(minFitness, chromosome.calculatedFitness)
      maxFitness = Math.max(maxFitness, chromosome.calculatedFitness)
    }
  }

  def resortChromosomes(): Unit = {
    chromosomes = chromosomes.sorted(Ordering.by((chromosome: Chromosome) => chromosome.calculatedFitness).reverse)
  }

  def getStatistics: PopulationStatistics = {
    PopulationStatistics(
      averageFitness = (totalFitness / chromosomes.length).toFloat,
      minFitness = minFitness,
      maxFitness = maxFitness,
      populationSize = chromosomes.length)
  }
}