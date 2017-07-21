package com.obecto.gattakka

object messages {

  object population {

    case class ReceiveSignal(data: AnyVal)

    case object RefreshPopulation

  }

  object individual {

    case class Initialize(environmentalData: AnyVal)

  }

  object evaluator {

    case object GetEvaluationAgent
    case object KillEvaluationAgent
  }

}
