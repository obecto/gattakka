package com.obecto.gattakka.actors

import com.obecto.gattakka.operators._
import com.obecto.gattakka.genetics._

import akka.actor.{ Actor }
import akka.pattern.{ ask }
import akka.util.{ Timeout }
import scala.concurrent.duration._
import scala.collection.TraversableOnce

class CreationActor extends Actor {
  import context.dispatcher
  import messages._
  import messages.creator._

  var generators: List[(ChromosomeGenerator, Float)] = List.empty
  var targetPopulationSize: Int = 10

  private var recreateIndividualsDeferred = false
  val recreateIndividualsDeferTimeout = 0.2.seconds
  implicit val timeout = Timeout(1.seconds)

  def receive = {
    case AddGenerator(generator, weigth) =>
      generators = (generator, weigth) :: generators

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
    for (population.PopulationResult(currentPopulation) <- context.parent.ask(population.GetPopulation);
        population.AliveCountResult(aliveCount) <- context.parent.ask(population.GetAliveCount)) {

      val neededAmount = targetPopulationSize - aliveCount

      val usableGenerators = generators.filter({
        case (generator, _) => generator.parentCount <= aliveCount
      })
      val totalWeigth = usableGenerators.foldLeft(0.0)((current, generator) => current + generator._2)

      val selected: Double = totalWeigth * scala.util.Random.nextDouble()
      var reached: Double = 0.0
      val selectedGenerator = usableGenerators.find((generator) => {
        reached = reached + generator._2
        (reached >= selected)
      }).getOrElse(usableGenerators.head)._1

      val builder = TraversableOnce.OnceCanBuildFrom[Chromosome]()
      var accumulatedSize = 0
      while (accumulatedSize < neededAmount) {
        val generated = selectedGenerator.apply(currentPopulation)
        accumulatedSize += generated.size
        builder ++= generated
      }


      val chromosomes = builder.result
      context.parent ! population.CreateIndividuals(chromosomes)
    }
  }
}
