package com.obecto.actors
import com.obecto.genetics._
import com.obecto.operators._

object messages {
  case class NewGeneration(generationId: Int)

  case object GetGeneration
  case class GenerationResult(generation: GenetationDetails)

  case class ApplyPipeline(method: Function[Pipeline, Int])

  case object StartGeneticAlgorithm
  case object StopGeneticAlgorithm
  case class SetPopulationSize(size: Int)

  case class GetEvolvedChromosomes(amount: Int)
  case class EvolvedChromosomesResult(chromosomes: Array[Int]) // TODO: switch to chromosome

  object internal {

    case class InitializeIndividual(chromosome: Int, data: AnyRef) // TODO: switch to chromosome
    case object IndividualReady

    case class GetFitness()
    case class FitnessResult(result: Float)

  }
}