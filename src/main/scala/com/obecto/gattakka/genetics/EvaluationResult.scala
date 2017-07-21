package com.obecto.gattakka.genetics

import akka.actor.ActorRef

import scala.collection.mutable.ArrayBuffer

class EvaluationResult(populationActor: ActorRef, var chromosomes: ArrayBuffer[Chromosome] = ArrayBuffer[Chromosome]()) {
  var totalFitness: Double = 0.0
  var minFitness: Float = Float.MaxValue
  var maxFitness: Float = Float.MinValue

  def addEvaluatedChromosome(chromosome : Chromosome, isLast : Boolean = false): Unit ={
    chromosomes.+=(chromosome)
  //  println("Adding evaluated result... " + chromosomes)
    if(isLast && chromosomes.nonEmpty){
      recomputeValues()
      //notifyEvaluationResultReady()
    }
  }

  def clear(): Unit ={
    if(chromosomes.nonEmpty){
      chromosomes.clear()
       totalFitness = 0.0
       minFitness = Float.MaxValue
       maxFitness = Float.MinValue
    }
  }

 /* def notifyEvaluationResultReady(): Unit ={
    populationActor ! EvaluationResult(chromosomes)
  }*/

  def recomputeValues(): Unit = {
    resortChromosomes()
    totalFitness = 0.0
    minFitness = Float.MaxValue
    maxFitness = Float.MinValue
    for(chromosome <- chromosomes) {
      totalFitness += chromosome.fitness
      minFitness = Math.min(minFitness, chromosome.fitness)
      maxFitness = Math.max(maxFitness, chromosome.fitness)
    }
  }

  def resortChromosomes(): Unit = {
    chromosomes = chromosomes.sorted(Ordering.by((chromosome: Chromosome) => chromosome.fitness).reverse)
  }

  def getStatistics: PopulationStatistics = {
    PopulationStatistics(
      averageFitness = (totalFitness / chromosomes.length).toFloat,
      minFitness = minFitness,
      maxFitness = maxFitness,
      populationSize = chromosomes.length)
  }
}

