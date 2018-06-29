package com.obecto.gattakka

import akka.actor.{Actor, ActorRef, PoisonPill, Props, Terminated}
import akka.pattern._
import com.obecto.gattakka.genetics.Genome
import com.obecto.gattakka.messages.evaluator.{GetAllFitnesses, RemoveFitness}
import com.obecto.gattakka.messages.eventbus.{AddSubscriber}
import com.obecto.gattakka.messages.individual.{Initialize, FitnessProducedEvent}
import com.obecto.gattakka.messages.population._

import scala.collection.mutable.HashMap
import scala.concurrent.{Await, Future}

object Population {
  def props(individualActorType: Class[_ <: Individual],
            initialGenomes: List[Genome],
            evaluator: ActorRef,
            pipelineActor: ActorRef,
            environmentalData: Any = null
           ): Props = {
    Props(classOf[Population], individualActorType,
      initialGenomes, evaluator, pipelineActor, environmentalData)
  }
}

class Population(
                  individualActorType: Class[_ <: Individual],
                  initialGenomes: List[Genome],
                  evaluator: ActorRef,
                  pipelineActor: ActorRef,
                  environmentalData: Any = null)
  extends Actor {

  case class IndividualData(genome: Genome, individualRef: ActorRef) {
    var fitness: Double = Double.NaN
  }


  implicit val timeout = Config.REQUEST_TIMEOUT

  import context.dispatcher

  private var pipelineRefreshesQueued = 0
  private var populationAge = 0
  private var isPipelineFree = true
  private var individualCounterId = 0
  private val lookupBusImpl = new LookupBusImplementation(self)
  private val currentIndividualData = new HashMap[String, IndividualData]


  evaluator ! IntroducePopulation
  hatchPopulation(initialGenomes.map(genome => IndividualDescriptor(genome)))


  def customReceive: PartialFunction[Any, Unit] = PartialFunction.empty[Any, Unit]

  def receive: Receive = customReceive orElse {

    case AddSubscriber(subscriber, classification) =>
      lookupBusImpl.subscribe(subscriber, classification)

    case RefreshPopulation(shouldQueue) =>
      if (isPipelineFree) {
        Await.ready(updateFitnesses(), timeout.duration)
        runPipeline()
        while (pipelineRefreshesQueued > 0) {
          pipelineRefreshesQueued -= 1
          runPipeline()
        }
      } else {
        if (shouldQueue) {
          pipelineRefreshesQueued += 1
          println(s"Pipeline is already running (queued ${pipelineRefreshesQueued} times)")
        }
      }

    case Terminated(ref) =>
      val id = ref.path.name
      evaluator ! RemoveFitness(id)
      currentIndividualData.remove(id) // Individual dies, so we don't really care about him anymore
  }

  protected def hatchPopulation(descriptors: List[IndividualDescriptor]) = {
    for (descriptor <- descriptors) yield {
      val id = descriptor.id getOrElse {
        generateUniqueId()
      }

      if (!currentIndividualData.contains(id)) {
        val individual = spawnIndividual(id, descriptor.genome)
        // val evaluationAgent = spawnEvaluationAgent(individual)
        // individual ! AddSubscriber(evaluationAgent, "individual_signal")
        individual ! Initialize(environmentalData)

        currentIndividualData(id) = IndividualData(descriptor.genome, individual)
      }
    }
  }

  private def spawnIndividual(id: String, genome: Genome): ActorRef = {
    val individual = context.actorOf(Props.apply(individualActorType, genome), id)
    context.watch(individual)
    individual ! AddSubscriber(evaluator, classOf[FitnessProducedEvent])
    individual
  }

  protected def runPipeline(): Future[Unit] = {
    println(s"Current population size is: ${currentIndividualData.size}")
    isPipelineFree = false

    val snapshot = currentIndividualData.view.map {
      case (id, data) => IndividualDescriptor(data.genome, Some(id), data.fitness)
    }.toList

    for {
      processedSnapshot <- (pipelineActor ? RunPipeline(snapshot)).mapTo[List[IndividualDescriptor]]
    } yield {
      val (doomedIndividuals, nonDoomedIndividuals) = processedSnapshot.partition(_.state == IndividualState.DoomedToDie)

      killIndividuals(doomedIndividuals)

      val newIndividuals = nonDoomedIndividuals.filter(_.id == None)
      println(s"Got ${newIndividuals.size} new individuals")
      val newPopulationDiversity = calculateDiversityPercentage(newIndividuals)

      hatchPopulation(newIndividuals)

      populationAge += 1
      println(s"Population aged one more year: $populationAge")
      println(f"Diversity: $newPopulationDiversity%.2f%% \n")

      lookupBusImpl.publish(PipelineFinishedEvent(currentIndividualData.size, newIndividuals.size))

      isPipelineFree = true
    }
  }

  protected def updateFitnesses(): Future[Unit] = {
    for {
      fitnesses <- (evaluator ? GetAllFitnesses).mapTo[Map[String, Double]]
    } yield {
      for ((id, fitness) <- fitnesses) {
        currentIndividualData(id).fitness = fitness
      }

    }
  }

  protected def killIndividuals(doomedDescriptors: List[IndividualDescriptor]): Unit = {
    for {
      doomedDescriptor <- doomedDescriptors
      id <- doomedDescriptor.id
      removedData <- currentIndividualData.remove(id) // Returns Option with the individual data
    } {
      removedData.individualRef ! PoisonPill
    }
  }

  private def generateUniqueId(): String = {
    individualCounterId += 1
    individualCounterId.toString
  }

  private def calculateDiversityPercentage(population: List[IndividualDescriptor]): Double = {
    val population_combinations = population.combinations(2)
    var population_combinations_length = 0
    var differenceSum: Double = 0.0

    while (population_combinations.hasNext) {
      population_combinations_length += 1
      val nextTuple = population_combinations.next()
      val difference = nextTuple(0).genome.diversity(nextTuple(1).genome)
      differenceSum += difference
    }

    differenceSum / population_combinations_length
  }

}
