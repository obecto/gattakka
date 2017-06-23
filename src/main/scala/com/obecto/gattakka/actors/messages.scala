package com.obecto.gattakka.actors
import com.obecto.gattakka.genetics._
import com.obecto.gattakka.operators._
import akka.actor.{ ActorRef }

object messages {
  object population {
    case object StartGeneticAlgorithm
    case object StopGeneticAlgorithm

    case class CreateIndividuals(chromosomes: TraversableOnce[Chromosome])
    case class KillIndividuals(chromosomes: TraversableOnce[Chromosome])

    case class SelectIndividuals(strategy: SelectionStrategy, amount: Int)
    case class SelectIndividualsPercent(strategy: SelectionStrategy, percent: Float)
    case class IndividualsResult(individuals: Seq[Chromosome])

    case object GetPopulation
    case class PopulationResult(population: Population)

    case object GetStatistics
    case class StatisticsResult(statistics: PopulationStatistics)

    case object GetAliveCount
    case class AliveCountResult(count: Int)

    case object PopulationSizeChangedEvent
  }

  object evaluator {
    case class GetEvaluatedPopulation()
    case class EvaluatedPopulationResult(result: Population)

    case class IntroduceIndividual(chromosome: Chromosome, individual: ActorRef)
  }

  object individual {
    case class Initialize(chromosome: Chromosome, evaluator: ActorRef)
    case object InitializeResult
  }

  object destructor {
    case class SetParameters(strategy: SelectionStrategy, killPercentage: Float)
    case class TrimPopulationSize(size: Int)
    case object KillIndividuals
  }

  object creator {
    case class SetPipeline(pipeline: Pipeline)
    case class SetTargetPopulationSize(size: Int)
  }
}
