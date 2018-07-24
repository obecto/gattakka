package com.obecto.gattakka

import akka.actor.ActorRef

object messages {

  object population {
    case class RunPipeline(snapshot: List[IndividualDescriptor])
    case class RefreshPopulation(queue: Boolean = true)
    case object IntroducePopulation

    case class PipelineFinishedEvent(populationSize: Int, newIndividualsSize: Int)
    case object GetCurrentPopulation
  }

  object individual {
    case class Initialize(environmentalData: Any)
    case class FitnessProducedEvent(fitness: Double)
    case class ProcessStartedEvent()
  }

  object evaluator {
    case class SetFitness(id: String, fitness: Double)
    case class RemoveFitness(id: String)
    case class GetFitness(id: String)
    case object GetAllFitnesses
  }

  object eventbus {
    case class AddSubscriber(subscriber: ActorRef, classification: Class[_])
  }

}
