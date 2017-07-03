package com.obecto.gattakka.actors
import akka.actor.{Actor}

class IndividualActor extends Actor {
  import messages.individual._

  def customReceive: PartialFunction[Any, Unit] = PartialFunction.empty[Any, Unit]

  def receive = customReceive orElse {
    case Initialize(chromosome, evaluator) =>
      context.sender ! InitializeResult

    case unrelatedMessage => println("Got a message: " + unrelatedMessage)

  }
}
