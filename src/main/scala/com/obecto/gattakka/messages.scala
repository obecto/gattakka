package com.obecto.gattakka

import akka.actor.ActorRef

object messages {

  object population {

    case class ReceiveSignal(data: AnyVal)

    case class RunPipeline(snapshot: List[IndividualDescriptor])

    case object RefreshPopulation

    case object PipelineFinished

    case object IntroducePopulation

  }

  object individual {

    case class Initialize(environmentalData: Any)

  }

  object evaluation {

    case object GetFitness
    case class GetEvaluationAgent(id: String)
    case class SpawnEvaluationAgent(id: String)
    case object KillEvaluationAgent
  }

  object eventbus {

    case class AddSubscriber(subscriber: ActorRef, classification: String)

    case class HandleEvent(dataType: String, payload: Any)

  }

}
