package com.obecto.gattakka.actors

import com.obecto.gattakka.operators._

import akka.actor.{ Actor }
import akka.pattern.{ ask }
import akka.util.{ Timeout }
import scala.concurrent.duration._

class CreationActor extends Actor {
  import context.dispatcher
  import messages._
  import messages.creator._

  var pipeline: Pipeline = Pipeline.empty
  var targetPopulationSize: Int = 10

  private var recreateIndividualsDeferred = false
  val recreateIndividualsDeferTimeout = 0.2.seconds
  implicit val timeout = Timeout(1.seconds)

  def receive = {
    case SetPipeline(newPipeline) =>
      pipeline = newPipeline

    case SetTargetPopulationSize(newTargetPopulationSize) =>
      targetPopulationSize = newTargetPopulationSize
      deferRecreateIndividuals()

    case population.PopulationSizeChangedEvent =>
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
    for (population.PopulationResult(result) <- context.parent.ask(population.GetPopulation);
        population.AliveCountResult(aliveCount) <- context.parent.ask(population.GetAliveCount)) {

      val neededAmount = targetPopulationSize - aliveCount
      val chromosomes = pipeline.apply(result, neededAmount)
      context.parent ! population.CreateIndividuals(chromosomes)
    }
  }
}
