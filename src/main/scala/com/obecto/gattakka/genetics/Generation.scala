package com.obecto.genetics
import scala.collection.mutable

class Generation(val sequentialId: Int, var chromosomes: mutable.ArrayBuffer[Chromosome] = mutable.ArrayBuffer[Chromosome]()) {
  def getGenerationDetails: GenetationDetails = {
    var totalFitness: Double = 0
    var minFitness: Float = Float.MaxValue
    var maxFitness: Float = Float.MinValue

    for (chromosome <- chromosomes) {
      totalFitness += chromosome.calculatedFitness
      minFitness = Math.min(minFitness, chromosome.calculatedFitness)
      maxFitness = Math.max(maxFitness, chromosome.calculatedFitness)
    }

    val averageFitness: Float = (totalFitness / chromosomes.length).toFloat

    GenetationDetails(sequentialId = sequentialId,
      averageFitness = averageFitness,
      minFitness = minFitness,
      maxFitness = maxFitness,
      populationSize = chromosomes.length)
  }
}