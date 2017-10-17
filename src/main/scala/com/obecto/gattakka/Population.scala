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

  import context.dispatcher
  val lookupBusImpl = new LookupBusImplementation
  val firstIndividualDescriptors: List[IndividualDescriptor] = initialGenomes map {
    genome: Genome =>
      IndividualDescriptor(genome, None)
  }
  private val currentIndividualDescriptors: ListBuffer[IndividualDescriptor] =
    hatchPopulation(firstIndividualDescriptors, evaluator).to[ListBuffer]
  var needToRefreshPipeline: Int = 0
  var populationAge = 0
  private var isPipelineFree = true

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
      println(needToRefreshPipeline)
      println("Pipeline is running")
    } else {
      runPipeline(currentIndividualDescriptors.toList)
    }
  }

  private def runPipeline(snapshot: List[IndividualDescriptor]): Unit = {
    setFitnesses()
    println(s"Population size is: ${currentIndividualDescriptors.size}")

    isPipelineFree = false
    val pipelineFuture = pipelineActor ? RunPipeline(snapshot)
    pipelineFuture.foreach({
      result =>
        val changedGenomes = result.asInstanceOf[List[IndividualDescriptor]]
        killIndividuals()
        println("childGenomes are: " + s"${changedGenomes.size}")
        val newIndividualDescriptors = hatchPopulation(changedGenomes, evaluator)
        currentIndividualDescriptors.++=:(newIndividualDescriptors)
        currentIndividualDescriptors foreach {
          _.tempParams.clear()
        }
        lookupBusImpl.publish(HandleEvent("pipeline_finished", PipelineFinished))
        populationAge += 1
        println(s"Population aged one more year: $populationAge")
        isPipelineFree = true
    })
  }

  private def setFitnesses(): Unit = {
    currentIndividualDescriptors foreach {
      desc =>
        desc.individualEvaluationPair match {
          case Some(pair) =>
            desc.currentFitness = getFitness(pair.evaluationAgent)
          case None =>
        }
    }
  }

  private def getFitness(evaluationAgent: ActorRef): Float = {
    try {
      val fitnessFuture = evaluationAgent ? GetFitness
      Await.result(fitnessFuture, timeout.duration).asInstanceOf[Float]
    } catch {
      //TODO fix this exception
      case exc: Exception =>
        exc.printStackTrace()
        throw exc
    }
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

  def getEvaluationAgent(individual: ActorRef, evaluator: ActorRef)(implicit timeout: Timeout): ActorRef = {
    try {
      val evaluationAgentFuture = evaluator ? GetEvaluationAgent
      val evaluationAgent = Await.result(evaluationAgentFuture, timeout.duration).asInstanceOf[ActorRef]
      evaluationAgent
    } catch {
      case exc: Exception => throw exc
    }
  }

  def createSubscription(to: ActorRef, subscriber: ActorRef, classification: String = ""): Unit = {
    to ! AddSubscriber(subscriber, classification)
  }

  def initializeNewbornIndividual(individual: ActorRef): ActorRef = {
    individual ! Initialize(environmentalData)
    individual
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

