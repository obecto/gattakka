package com.obecto.gattakka.actors

import com.obecto.gattakka.operators._

import akka.actor.{ Actor }
import akka.pattern.{ ask }
import akka.util.{ Timeout }
import scala.concurrent.duration._
import scala.util.{ Success }


class DestructionActor extends Actor {
  import context.dispatcher
  import messages._
  import messages.destructor._

  var strategy: SelectionStrategy = null
  var killPercentage: Float = 0.5f
  implicit val timeout = Timeout(1.seconds)

  def receive = {
    case SetParameters(newStrategy, newKillPercentage) =>
      strategy = newStrategy
      killPercentage = newKillPercentage

    case TrimPopulationSize(newSize) =>
      for (population.IndividualsResult(individuals) <- context.parent.ask(population.SelectIndividuals(strategy, - newSize))) {
        context.parent ! population.KillIndividuals(individuals)
      }


    case KillIndividuals =>
      for (population.IndividualsResult(individuals) <- context.parent.ask(population.SelectIndividualsPercent(strategy, killPercentage))) {
        context.parent ! population.KillIndividuals(individuals)
      }

    case unrelatedMessage => println("Got a message: " + unrelatedMessage)
  }
}
