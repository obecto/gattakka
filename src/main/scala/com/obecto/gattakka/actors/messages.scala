package com.obecto.actors
import com.obecto.genetics._
import com.obecto.operators._
import akka.actor.{ ActorRef }

object messages {
  case class NewPopulation(populationId: Int)

  case object GetStatistics
  case class StatisticsResult(population: PopulationStatistics)

  case class GetEvolvedChromosomes(strategy: SelectionStrategy, amount: Int)
  case class EvolvedChromosomesResult(chromosomes: Seq[Chromosome])

  case class ChangePipeline(newPipeline: Pipeline)

  case class KillIndividual(ref: ActorRef)

  case object StartGeneticAlgorithm
  case object StopGeneticAlgorithm
  case class SetTargetPopulationSize(size: Int, shouldKill: Boolean = false)


  case class InitializeIndividual(chromosome: Chromosome, evaluator: ActorRef)
  case object IndividualReady

  case class IntroduceIndividual(chromosome: Chromosome, individual: ActorRef)

  case class GetEvaluatedFitnesses()
  case class EvaluatedFitnessesResult(result: Map[ActorRef, Float])

}
