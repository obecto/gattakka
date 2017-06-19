package com.obecto.gattakka.actors

import com.obecto.gattakka.operators._

import akka.actor.{ Actor }
import akka.pattern.{ ask }
import akka.util.{ Timeout }
import scala.concurrent.duration._
import scala.util.{ Success }

class CreationActor extends Actor {
  import context.dispatcher
  import messages._
  import messages.creator._

  var pipeline: Pipeline = Pipeline.empty
  var targetPopulationSize: Int = 10

  private var recreateIndividualsDeferred = false
  val recreateIndividualsDeferTimeout = 0.5.seconds
  implicit val timeout = Timeout(1.seconds)

  def customReceive: PartialFunction[Any, Unit] = PartialFunction.empty[Any, Unit]

  def receive = customReceive orElse {
    case SetPipeline(newPipeline) =>
      pipeline = newPipeline

    case SetTargetPopulationSize(newTargetPopulationSize) =>
      targetPopulationSize = newTargetPopulationSize
      deferRecreateIndividuals()

    case unrelatedMessage => println("Got a message: " + unrelatedMessage)
  }


  def deferRecreateIndividuals(): Unit = {
    if (!recreateIndividualsDeferred) {
      recreateIndividualsDeferred = true
      context.system.scheduler.scheduleOnce(recreateIndividualsDeferTimeout) {
        recreateIndividualsDeferred = false
        recreateIndividuals()
      }
    }
  }

  def recreateIndividuals(): Unit = {
    context.parent.ask(population.GetPopulation) andThen {

      case Success(population.PopulationResult(result)) =>
        val neededAmount = targetPopulationSize - result.chromosomes.size
        val chromosomes = pipeline.apply(result, neededAmount)
        context.parent ! population.CreateIndividuals(chromosomes)

    }
  }
}
