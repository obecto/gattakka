package com.obecto.genetics
import scala.collection.mutable

class Generation(
    val sequentialId: Int,
    val targetPopulationSize: Int = 20,
    var chromosomes: mutable.ArrayBuffer[Chromosome] = mutable.ArrayBuffer[Chromosome]()) {

  private class LazyComputer {
    lazy val totalFitness: Double = {
      chromosomes.foldLeft(0.0)(
        (current: Double, chromosome: Chromosome) => current + chromosome.calculatedFitness)
    }
    lazy val minFitness: Float = {
      chromosomes.foldLeft(Float.MaxValue)(
        (current: Float, chromosome: Chromosome) => Math.min(current, chromosome.calculatedFitness))
    }
    lazy val maxFitness: Float = {
      chromosomes.foldLeft(Float.MinValue)(
        (current: Float, chromosome: Chromosome) => Math.max(current, chromosome.calculatedFitness))
    }
  }

  private var lazyVals = new LazyComputer()

  def totalFitness = lazyVals.totalFitness
  def minFitness = lazyVals.minFitness
  def maxFitness = lazyVals.maxFitness

  def recomputeValues(): Unit = {
    lazyVals = new LazyComputer()
  }

  def resortChromosomes(): Unit = {
    chromosomes = chromosomes.sorted(Ordering.by((chromosome: Chromosome) => chromosome.calculatedFitness).reverse)
  }

  def getGenerationDetails: GenetationDetails = {
    GenetationDetails(sequentialId = sequentialId,
      averageFitness = (totalFitness / chromosomes.length).toFloat,
      minFitness = minFitness,
      maxFitness = maxFitness,
      populationSize = chromosomes.length)
  }
}