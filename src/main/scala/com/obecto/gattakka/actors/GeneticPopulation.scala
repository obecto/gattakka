package com.obecto.gattakka.actors

import com.obecto.gattakka.genetics.{ Chromosome, Population }

import akka.actor.{ Actor, ActorRef, Props, Terminated }
import akka.pattern.{ ask, pipe }
import akka.util.{ Timeout }
import scala.collection.mutable
import scala.concurrent.duration._
import scala.concurrent.Future

object GeneticPopulationActor {
  def getProps(evaluatorProps: Props,
      individualProps: Props,
      creatorProps: Props = Props[CreationActor],
      destructorProps: Props = Props[DestructionActor]
    ): Props = {
    Props(classOf[GeneticPopulationActor], evaluatorProps, individualProps, creatorProps, destructorProps)
  }
}

class GeneticPopulationActor(evaluatorProps: Props, individualProps: Props, creatorProps: Props, destructorProps: Props) extends Actor {
  import context.dispatcher
  import messages.population._

  case class IndividualRef(state: IndividualState, actor: ActorRef = null)

  sealed trait IndividualState {
    var count = 0
  }
  case object Initializing extends IndividualState
  case object Initialized extends IndividualState



  val childChromosomes = mutable.HashMap[ActorRef, Chromosome]()
  val individualRefs = mutable.HashMap[Chromosome, IndividualRef]()

  val evaluator = context.actorOf(evaluatorProps, "evaluator")
  val creator = context.actorOf(creatorProps, "creator")
  val destructor = context.actorOf(destructorProps, "destructor")

  var nextSequentialId = 0

  def receive = {
    case KillIndividuals(individuals) =>
      for (individual <- individuals) {
        if (individualRefs.contains(individual)) {
          context.stop(individualRefs(individual).actor)
        }
      }

    case CreateIndividuals(individuals) =>
      for (individual <- individuals) {
        createIndividual(individual)
      }

    case SelectIndividuals(strategy, amount) =>
      fetchEvaluatedPopulation() map { population =>
          population.resortChromosomes()
          population.recomputeValues()

          if (amount < 0)
            IndividualsResult(strategy.apply(population, amount + population.chromosomes.size))
          else
            IndividualsResult(strategy.apply(population, amount))

      } pipeTo context.sender

    case SelectIndividualsPercent(strategy, percent) =>
      fetchEvaluatedPopulation() map { population =>
          population.resortChromosomes()
          population.recomputeValues()
          IndividualsResult(strategy.apply(population, (population.chromosomes.size * percent).round))
      } pipeTo context.sender

    case GetStatistics =>
      fetchEvaluatedPopulation() map { population =>
        StatisticsResult(population.getStatistics)
      } pipeTo context.sender

    case GetAliveCount =>
      context.sender ! AliveCountResult(individualRefs.size)

    case GetPopulation =>
      fetchEvaluatedPopulation() map { population =>
        PopulationResult(population)
      } pipeTo context.sender

    case messages.individual.InitializeResult =>
      setChildState(childChromosomes(context.sender), IndividualRef(Initialized))
      evaluator ! messages.evaluator.IntroduceIndividual(childChromosomes(context.sender), context.sender)
      // println("One more readied")

    case Terminated(individual) =>
      setChildState(childChromosomes(individual), null)
      creator ! PopulationSizeChangedEvent
      // println("Gah, he died")

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

    // println("Initializing an individual...")
    setChildState(chromosome, IndividualRef(Initializing, individual))
    individual ! messages.individual.Initialize(chromosome, evaluator)
    evaluator ! messages.evaluator.IntroduceIndividual(chromosome, individual)
  }


  private def setChildState(chromosome: Chromosome, newRef: IndividualRef): Unit = {
    individualRefs.get(chromosome).map { ref =>
      ref.state.count -= 1
    }

    if (newRef != null) {
      newRef.state.count += 1
      val oldRef = individualRefs.getOrElse(chromosome, null)
      if (newRef.actor != null) {
        if (oldRef != null) childChromosomes.remove(oldRef.actor)
        childChromosomes(newRef.actor) = chromosome
        individualRefs(chromosome) = IndividualRef(newRef.state, newRef.actor)
      } else if (oldRef != null) {
        individualRefs(chromosome) = IndividualRef(newRef.state, oldRef.actor)
      }
    } else {
      individualRefs.remove(chromosome)
    }
  }
}
