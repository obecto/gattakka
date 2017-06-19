package com.obecto.gattakka.actors

import com.obecto.gattakka.operators._
import com.obecto.gattakka.genetics._

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
      // TODO: Should we support negative amounts?
      context.parent.ask(population.SelectIndividuals(strategy, -newSize)) andThen {

        case Success(population.IndividualsResult(individuals)) =>
          context.parent ! population.KillIndividuals(individuals)

      }


    case KillIndividuals =>
      context.parent.ask(population.SelectIndividualsPercent(strategy, killPercentage)) andThen {

        case Success(population.IndividualsResult(individuals)) =>
          context.parent ! population.KillIndividuals(individuals)

      }
      //targetPopulationSize = newTargetPopulationSize

    case unrelatedMessage => println("Got a message: " + unrelatedMessage)
  }
}
