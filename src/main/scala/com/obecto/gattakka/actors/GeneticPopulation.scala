package com.obecto.gattakka.actors
import scala.collection.mutable
import com.obecto.gattakka.genetics.{ Chromosome, Population }
import com.obecto.gattakka.operators.{ Pipeline }
import akka.actor.{ Actor, ActorRef, Props, Terminated }
import akka.pattern.{ ask, pipe }
import akka.util.{ Timeout }
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.{ Success, Failure }

class GeneticPopulationActor(evaluatorProps: Props, individualProps: Props, pipeline: Pipeline) extends Actor {
  import context.dispatcher
  import messages._

  sealed trait ChildState
  case object Initializing extends ChildState
  case object Initialized extends ChildState

  var targetSize = 10
  var running = false

  val evaluator = context.actorOf(evaluatorProps, "evaluator")
  val individualStates = mutable.HashMap[ActorRef, (ChildState, Chromosome)]()
  var initializedCount = 0
  var initializingCount = 0
  var individualCount = 0
  var nextSequentialId = 0

  private var recreateIndividualsDeferred = false
  val recreateIndividualsDeferTimeout = 0.5.seconds

  def receive = {
    case StartGeneticAlgorithm =>
      running = true
      recreateIndividuals()

    case StopGeneticAlgorithm =>
      running = false

    case SetTargetPopulationSize(newSize, shouldKill) =>
      if (shouldKill && targetSize > newSize) {
        targetSize = newSize
        killExcessIndividuals()
      } else {
        targetSize = newSize
        recreateIndividuals()
      }

    case KillIndividual(individual) =>
      setChildState(context.sender, null)
      context.stop(individual)

    case GetEvolvedChromosomes(strategy, amount) =>
      fetchEvaluatedPopulation() andThen {
        case Success(population) =>
          population.resortChromosomes()
          population.recomputeValues()
          EvolvedChromosomesResult(strategy.apply(population, amount))
      } pipeTo context.sender

    case GetStatistics =>
      fetchEvaluatedPopulation() andThen {
        case Success(population) =>
          StatisticsResult(population.getStatistics)
      } pipeTo context.sender


    case IndividualReady =>
      setChildState(context.sender, Initialized)
      evaluator ! IntroduceIndividual(individualStates(context.sender)._2, context.sender)
      println("One more readied")

    case Terminated(individual) =>
      setChildState(context.sender, null)
      println("Gah, he died!")
      deferRecreateIndividuals()

    case unrelatedMessage => println("Got a message: " + unrelatedMessage)
  }


  def fetchEvaluatedPopulation(): Future[Population] = {
    val evaluatedFitnessesFuture = (evaluator ? GetEvaluatedFitnesses) (Timeout(1.seconds))
    val populationFuture = evaluatedFitnessesFuture map {
      case EvaluatedFitnessesResult(result) =>
        val mappedResults = result.map({
          case (individual, fitness) =>
            if (individualStates.contains(individual)) {
              (individualStates(individual)._2, fitness)
            } else {
              (null, 0f)
            }
        })

        Population.from(mappedResults)
    }
    return populationFuture.mapTo[Population]
  }


  def deferRecreateIndividuals(): Unit = {
    if (!recreateIndividualsDeferred) {
      recreateIndividualsDeferred = true
      println("GO IN")
      context.system.scheduler.scheduleOnce(recreateIndividualsDeferTimeout) {
        println("GO ON")
        recreateIndividualsDeferred = false
        recreateIndividuals()
      }
    }
  }

  def recreateIndividuals(): Unit = {
    if (running) {
      fetchEvaluatedPopulation() andThen {
        case Success(population) =>
          val neededAmount = targetSize - individualCount
          population.resortChromosomes()
          population.recomputeValues()
          val chromosomes = pipeline.apply(population, neededAmount)
          for (chromosome <- chromosomes) {
            createIndividual(chromosome)
          }
        case Failure(exception) => println(exception.getStackTraceString)
      }
    }
  }

  def createIndividual(chromosome: Chromosome): Unit = {
    val individual = context.actorOf(individualProps, s"individual-$nextSequentialId")
    nextSequentialId += 1

    context.watch(individual)

    println("Initializing an individual...")
    setChildState(individual, Initializing, chromosome)
    individual ! InitializeIndividual(chromosome, evaluator)
  }

  def killExcessIndividuals(): Unit = {
    println("Killing..." + individualCount)
    for (i <- targetSize until individualCount) {
      setChildState(context.sender, null)
      val individual = individualStates.keys.toSeq(scala.util.Random.nextInt(individualStates.size))
      context.stop(individual)
    }
  }


  private def setChildState(individual: ActorRef, newState: ChildState, newChromosome: Chromosome = null): Unit = {
    individualStates.getOrElse(individual, null) match {
      case (Initialized, _) => initializedCount -= 1
      case (Initializing, _) => initializingCount -= 1
      case null => individualCount += 1
    }
    newState match {
      case Initialized => initializedCount += 1
      case Initializing => initializingCount += 1
      case null => individualCount -= 1
    }
    if (newState != null) {
      if (newChromosome != null) {
        individualStates(individual) = (newState, newChromosome)
      } else {
        individualStates(individual) = (newState, individualStates(individual)._2)
      }
    } else {
      individualStates.remove(individual)
    }
  }
}
