package com.obecto.actors
import akka.actor.{Actor}

class IndividualActor extends Actor {
  import messages._

  def customReceive: PartialFunction[Any, Unit] = ???

  def receive = customReceive orElse {
    case InitializeIndividual(chromosome, evaluator) =>
      for (i <- 0 to 1000) { // Pretend to do something
        for (j <- 0 to 2000) {
          if (i * j % 7 == 2 && (i - j) == 40) {
            println(i)
          }
        }
      }
      context.sender ! IndividualReady

    case unrelatedMessage => println("Got a message: " + unrelatedMessage)

  }
}
