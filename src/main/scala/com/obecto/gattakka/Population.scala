package com.obecto.gattakka

import akka.actor.{Actor, ActorRef, PoisonPill, Props, Terminated}
import akka.pattern._
import akka.util.Timeout
import com.obecto.gattakka.genetics.Genome
import com.obecto.gattakka.messages.evaluation.{GetEvaluationAgent, GetFitness}
import com.obecto.gattakka.messages.eventbus.{AddSubscriber, HandleEvent}
import com.obecto.gattakka.messages.individual.Initialize
import com.obecto.gattakka.messages.population.{PipelineFinished, RefreshPopulation, RunPipeline}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.Await

object Population {
  def props(individualActorType: Class[_ <: Individual],
            initialGenomes: List[Genome],
            evaluator: ActorRef,
            pipelineActor: ActorRef,
            environmentalData: Any = ""
           ): Props = {
    Props(classOf[Population], individualActorType,
      initialGenomes, evaluator: ActorRef, pipelineActor, environmentalData)
  }
}

class Population(individualActorType: Class[_ <: Individual],
                 initialGenomes: List[Genome],
                 evaluator: ActorRef,
                 pipelineActor: ActorRef,
                 environmentalData: Any = ""
                       ) extends Actor {


  implicit val timeout = Config.REQUEST_TIMEOUT

  val lookupBusImpl = new LookupBusImplementation
  val firstIndividualDescriptors: List[IndividualDescriptor] = initialGenomes map {
    genome: Genome =>
      IndividualDescriptor(genome, None)
  }
  var needToRefreshPipeline: Int = 0
  var populationAge = 0
  var isPipelineFree = true
  var currentIndividualDescriptors: ListBuffer[IndividualDescriptor] =
    hatchPopulation(firstIndividualDescriptors, evaluator).to[ListBuffer]

  def receive: Receive = {

    case AddSubscriber(subscriber, classification) =>
      lookupBusImpl.subscribe(subscriber, classification)

    case RefreshPopulation =>
      handleRefreshPipelineRequest()

    case Terminated(ref) =>
    //println("My child died :( " + ref.path.name)

  }

  def handleRefreshPipelineRequest(): Unit = {
    if (!isPipelineFree) {
      needToRefreshPipeline += 1
      println("Pipeline is running")
    } else {
      runPipeline(currentIndividualDescriptors.toList)
    }
  }


  def hatchPopulation(descriptors: List[IndividualDescriptor], evaluator: ActorRef)
                     (implicit timeout: Timeout): List[IndividualDescriptor] = {
    descriptors foreach {
      descriptor =>
        val individual = giveBirthToIndividual(descriptor.genome)
        val evaluationAgent = getEvaluationAgent(individual, evaluator)
        createSubscription(individual, evaluationAgent, "individual_signal")
        initializeNewbornIndividual(individual)
        descriptor.individualEvaluationPair = Some(IndividualEvaluationPair(individual, evaluationAgent))
    }
    descriptors
  }

  def giveBirthToIndividual(genome: Genome): ActorRef = {
    val individual = context.actorOf(Props.apply(individualActorType, genome))
    context.watch(individual)
    individual
  }

  def createSubscription(to: ActorRef, subscriber: ActorRef, classification: String = ""): Unit = {
    to ! AddSubscriber(subscriber, classification)
  }

  def getEvaluationAgent(individual: ActorRef, evaluator: ActorRef)(implicit timeout: Timeout): ActorRef = {
    try {
      val evaluationAgentFuture = evaluator ? GetEvaluationAgent
      val evaluationAgent = Await.result(evaluationAgentFuture, timeout.duration).asInstanceOf[ActorRef]
      evaluationAgent
    } catch {
      case exc: Exception => throw exc
    }
  }

  def initializeNewbornIndividual(individual: ActorRef): ActorRef = {
    individual ! Initialize(environmentalData)
    individual
  }

  def killIndividuals(): Unit = {
    currentIndividualDescriptors filter {
      individualDescriptor =>
        individualDescriptor.doomedToDie
    } foreach {
      doomedDescriptor =>
        doomedDescriptor.individualEvaluationPair match {
          case Some(individualEvaluationPair) =>
            individualEvaluationPair.individual ! PoisonPill
            individualEvaluationPair.evaluationAgent ! PoisonPill
            doomedDescriptor.individualEvaluationPair = None
          case None =>
        }
        currentIndividualDescriptors -= doomedDescriptor
    }
  }

  private def getFitness(evaluationAgent: ActorRef): Float = {
    try {
      val fitnessFuture = evaluationAgent ? GetFitness
      Await.result(fitnessFuture, timeout.duration).asInstanceOf[Float]
    } catch {
      case exc: Exception =>
        exc.printStackTrace()
        throw exc
    }
  }

  private def runPipeline(snapshot: List[IndividualDescriptor]): Unit = {
    currentIndividualDescriptors foreach {
      desc =>
        desc.individualEvaluationPair match {
          case Some(pair) =>
            desc.currentFitness = getFitness(pair.evaluationAgent)
          case None =>
        }
    }

    println(s"Population size is: ${currentIndividualDescriptors.size}")

    isPipelineFree = false
    try {
      val pipelineFuture = pipelineActor ? RunPipeline(snapshot)
      val childGenomes = Await.result(pipelineFuture, timeout.duration)
        .asInstanceOf[List[IndividualDescriptor]]
      killIndividuals()
      println("childGenomes are: " + s"${childGenomes.size}")
      val newIndividualDescriptors = hatchPopulation(childGenomes, evaluator)
      currentIndividualDescriptors.++=:(newIndividualDescriptors)
      currentIndividualDescriptors foreach {
        _.tempParams.clear()
      }
    } catch {
      case exc: Exception => exc.printStackTrace()
    }

    lookupBusImpl.publish(HandleEvent("pipeline_finished", PipelineFinished))
    populationAge += 1
    println(s"Population aged one more year: $populationAge")
    isPipelineFree = true

    if (needToRefreshPipeline > 0) {
      runPipeline(currentIndividualDescriptors.toList)
      needToRefreshPipeline -= 1
    }

  }

}

case class IndividualDescriptor(genome: Genome,
                                var individualEvaluationPair: Option[IndividualEvaluationPair],
                                var currentFitness: Float = 0.0f,
                                var doomedToDie: Boolean = false,
                                var retainGenome: Boolean = false,
                                additionalParams: mutable.Map[String, Any] = mutable.Map.empty[String, Any],
                                tempParams: mutable.Map[String, Any] = mutable.Map.empty[String, Any]
                               )

case class IndividualEvaluationPair(individual: ActorRef, evaluationAgent: ActorRef)

