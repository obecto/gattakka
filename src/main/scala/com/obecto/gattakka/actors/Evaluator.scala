package com.obecto.gattakka.actors
import akka.actor.{ Actor }

class EvaluatorActor extends Actor {
  import messages._

  def customReceive: PartialFunction[Any, Unit] = ???

  def receive = customReceive orElse {
    case IntroduceIndividual(chromosome, reference) =>

    case GetEvaluatedFitnesses =>
      assert(false, "You should reimplement EvaluatorActor with a proper implementation for GetEvaluatedFitnesses")

    case unrelatedMessage => println("Got a message: " + unrelatedMessage)
  }
}
