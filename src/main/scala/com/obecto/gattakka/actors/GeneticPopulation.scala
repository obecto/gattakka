package com.obecto.gattakka.actors

import com.obecto.gattakka.genetics.{ Chromosome, Population }
import com.obecto.gattakka.operators.{ Pipeline }

import akka.actor.{ Actor, ActorRef, Props, Terminated }
import akka.pattern.{ ask, pipe }
import akka.util.{ Timeout }
import scala.collection.mutable
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.{ Success }

class GeneticPopulationActor(evaluatorProps: Props, individualProps: Props, pipeline: Pipeline) extends Actor {
  import context.dispatcher
  import messages.population._

  case class IndividualRef(state: IndividualState, ref: ActorRef)

  sealed trait IndividualState {
    var count = 0
  }
  case object Initializing extends IndividualState
  case object Initialized extends IndividualState


  var targetSize = 10
  var running = false

  val evaluator = context.actorOf(evaluatorProps, "evaluator")
  val individualStates = mutable.HashMap[Chromosome, IndividualRef]()
  var initializedCount = 0
  var initializingCount = 0
  var individualCount = 0
  var nextSequentialId = 0

  def receive = {
    case StartGeneticAlgorithm =>
      running = true

    case StopGeneticAlgorithm =>
      running = false

    case SetTargetPopulationSize(newSize, shouldKill) =>
      if (shouldKill && targetSize > newSize) {
        targetSize = newSize
        // killExcessIndividuals()
      } else {
        targetSize = newSize
        // recreateIndividuals()
      }

    case KillIndividual(individual) =>
      context.stop(individualStates(individual).ref)
      setChildState(individual, null)

    case SelectIndividuals(strategy, amount) =>
      fetchEvaluatedPopulation() andThen {
        case Success(population) =>
          population.resortChromosomes()
          population.recomputeValues()
          IndividualsResult(strategy.apply(population, amount))
      } pipeTo context.sender

    case GetStatistics =>
      fetchEvaluatedPopulation() andThen {
        case Success(population) =>
          StatisticsResult(population.getStatistics)
      } pipeTo context.sender


    case messages.individual.InitializeResult =>
      // setChildState(context.sender, Initialized)
      // evaluator ! messages.evaluator.IntroduceIndividual(individualStates(context.sender), context.sender)
      // println("One more readied")

    case Terminated(individual) =>
      // setChildState(context.sender, null)
      // println("Gah, he died!")
      // deferRecreateIndividuals()

    case unrelatedMessage => println("Got a message: " + unrelatedMessage)
  }


  def fetchEvaluatedPopulation(): Future[Population] = {
    val evaluatedFitnessesFuture = (evaluator ? messages.evaluator.GetEvaluatedPopulation) (Timeout(1.seconds))
    val populationFuture = evaluatedFitnessesFuture map {
      case messages.evaluator.EvaluatedPopulationResult(result) => result
    }
    return populationFuture.mapTo[Population]
  }

  def createIndividual(chromosome: Chromosome): Unit = {
    val individual = context.actorOf(individualProps, s"individual-$nextSequentialId")
    nextSequentialId += 1

    context.watch(individual)

    println("Initializing an individual...")
    setChildState(chromosome, Initializing, individual)
    individual ! messages.individual.Initialize(chromosome, evaluator)
  }


  private def setChildState(individual: Chromosome, newState: IndividualState, newRef: ActorRef = null): Unit = {
    individualStates.getOrElse(individual, null) match {
      case IndividualRef(Initialized, _) => initializedCount -= 1
      case IndividualRef(Initializing, _) => initializingCount -= 1
      case null => individualCount += 1
    }
    newState match {
      case Initialized => initializedCount += 1
      case Initializing => initializingCount += 1
      case null => individualCount -= 1
    }
    if (newState != null) {
      if (newRef != null) {
        individualStates(individual) = IndividualRef(newState, newRef)
      } else {
        individualStates(individual) = IndividualRef(newState, individualStates(individual).ref)
      }
    } else {
      individualStates.remove(individual)
    }
  }
}
