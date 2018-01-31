package com.obecto.gattakka

import akka.actor.{Actor, ActorRef, PoisonPill, Props, Terminated}
import akka.pattern._
import akka.util.Timeout
import com.obecto.gattakka.genetics.Genome
import com.obecto.gattakka.messages.evaluation.{GetFitness, SpawnEvaluationAgent}
import com.obecto.gattakka.messages.eventbus.{AddSubscriber, HandleEvent}
import com.obecto.gattakka.messages.individual.Initialize
import com.obecto.gattakka.messages.population.{IntroducePopulation, PipelineFinished, RefreshPopulation, RunPipeline}

import scala.collection.immutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.Await

object Population {

  var BOT_COUNTER_ID: Long = 0

  def props(individualActorType: Class[_ <: Individual],
            initialGenomes: List[Genome],
            evaluator: ActorRef,
            pipelineActor: ActorRef,
            environmentalData: Any = ""
           ): Props = {
    Props(classOf[Population], individualActorType,
      initialGenomes, evaluator: ActorRef, pipelineActor, environmentalData)
  }

   def getUniqueBotId: String ={
    BOT_COUNTER_ID +=1
    BOT_COUNTER_ID.toString
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
  private var needToRefreshPipeline: Int = 0
  private var populationAge: Int = 0
  private var isPipelineFree: Boolean = true
  private var botCounterId: Long = 0

  evaluator ! IntroducePopulation

  private val lookupBusImpl = new LookupBusImplementation

  private val firstIndividualDescriptors: List[IndividualDescriptor] = initialGenomes map {
    genome: Genome =>
      IndividualDescriptor(Population.getUniqueBotId, genome, None)
  }

  private val currentIndividualDescriptors: ListBuffer[IndividualDescriptor] =
    hatchPopulation(firstIndividualDescriptors, evaluator).to[ListBuffer]

  def customReceive: PartialFunction[Any, Unit] = PartialFunction.empty[Any, Unit]

  def receive: Receive = customReceive orElse {

    case AddSubscriber(subscriber, classification) =>
      lookupBusImpl.subscribe(subscriber, classification)

    case RefreshPopulation =>
      handleRefreshPipelineRequest()

    case Terminated(ref) =>
    //println("My child died :( " + ref.path.name)

  }

  protected def handleRefreshPipelineRequest(): Unit = {
    if (!isPipelineFree) {
      needToRefreshPipeline += 1
      println(needToRefreshPipeline)
      println("Pipeline is running")
    } else {
      setFitnesses()
      runPipeline(makeSnapshot)
    }
  }

  protected def makeSnapshot: List[IndividualDescriptor] = {
    currentIndividualDescriptors.toList map (_.copy())
  }

  protected def runPipeline(snapshot: List[IndividualDescriptor]): Unit = {
    println(s"Population size is: ${currentIndividualDescriptors.size}")
    isPipelineFree = false
    val pipelineFuture = pipelineActor ? RunPipeline(snapshot)

    pipelineFuture.foreach({
      result =>
        val processedShapshot = result.asInstanceOf[List[IndividualDescriptor]]
        val weakSnapshotIndividuals = selectWeakIndividuals(processedShapshot)
        killIndividuals(weakSnapshotIndividuals)
        val mustLiveSnapshotIndividuals = processedShapshot.filterNot(weakSnapshotIndividuals contains _)
        val childrenSnapshotIndividuals = selectChildrenIndividuals(mustLiveSnapshotIndividuals)
        println("childGenomes are: " + s"${childrenSnapshotIndividuals.size}")
        val newIndividualDescriptors = hatchPopulation(childrenSnapshotIndividuals, evaluator)
        currentIndividualDescriptors ++= newIndividualDescriptors
        publishPipelineResult()
        populationAge += 1
        println(s"Population aged one more year: $populationAge")
        isPipelineFree = true
    })
  }

  private def selectWeakIndividuals(individualDescriptors: List[IndividualDescriptor]): List[IndividualDescriptor] = {
    individualDescriptors filter (_.doomedToDie)
  }

  private def selectChildrenIndividuals(processedSnapshot: List[IndividualDescriptor]): List[IndividualDescriptor] ={
    processedSnapshot filterNot {
      currentElement =>
        currentIndividualDescriptors.exists(_.genome.equals(currentElement.genome))
    }
  }

  protected def setFitnesses(): Unit = {
    currentIndividualDescriptors foreach {
      desc =>
        desc.individualEvaluationPair match {
          case Some(pair) =>
            desc.currentFitness = getFitness(pair.evaluationAgent)
          case None =>
        }
    }
  }

  protected def getFitness(evaluationAgent: ActorRef): Double = {
    try {
      val fitnessFuture = evaluationAgent ? GetFitness
      Await.result(fitnessFuture, timeout.duration).asInstanceOf[Double]
    } catch {
      //TODO fix this exception
      case exc: Exception =>
        exc.printStackTrace()
        throw exc
    }
  }

  private def killIndividuals(weakIndividuals: List[IndividualDescriptor]): Unit = {
    currentIndividualDescriptors filter {
      descriptor =>
        weakIndividuals.exists(_.genome.equals(descriptor.genome))
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

  protected def hatchPopulation(descriptors: List[IndividualDescriptor], evaluator: ActorRef)
                     (implicit timeout: Timeout): List[IndividualDescriptor] = {
    descriptors foreach {
      descriptor =>
        if(!isAlreadyHatched(descriptor)) {
          val individual = giveBirthToIndividual(descriptor.id,descriptor.genome)
          val evaluationAgent = spawnEvaluationAgent(individual, evaluator)
          createSubscription(individual, evaluationAgent, "individual_signal")
          initializeNewbornIndividual(individual)
          descriptor.individualEvaluationPair = Some(IndividualEvaluationPair(individual, evaluationAgent))
        }
    }
    descriptors
  }

  private def isAlreadyHatched(individualDescriptor: IndividualDescriptor): Boolean ={
    individualDescriptor.individualEvaluationPair.nonEmpty
  }

  private def giveBirthToIndividual(id: String, genome: Genome): ActorRef = {
    val individual = context.actorOf(Props.apply(individualActorType, genome), id)
    context.watch(individual)
    individual
  }

  private def spawnEvaluationAgent(individual: ActorRef, evaluator: ActorRef)(implicit timeout: Timeout): ActorRef = {
    try {
      val evaluationAgentFuture = evaluator ? SpawnEvaluationAgent(individual.path.name)
      val evaluationAgent = Await.result(evaluationAgentFuture, timeout.duration).asInstanceOf[ActorRef]
      evaluationAgent
    } catch {
      case exc: Exception => throw exc
    }
  }

  protected def createSubscription(to: ActorRef, subscriber: ActorRef, classification: String = ""): Unit = {
    to ! AddSubscriber(subscriber, classification)
  }

  protected def initializeNewbornIndividual(individual: ActorRef): ActorRef = {
    individual ! Initialize(environmentalData)
    individual
  }

  protected def publishPipelineResult(): Unit ={
    lookupBusImpl.publish(HandleEvent("pipeline_finished", PipelineFinished))

  }

}

case class IndividualDescriptor(
  id: String,
  genome: Genome,
  var individualEvaluationPair: Option[IndividualEvaluationPair],
  var currentFitness: Double = 0.0,
  var doomedToDie: Boolean = false,
  var retainGenome: Boolean = false,
  var additionalParams: immutable.Map[String, Any] = immutable.Map[String, Any](),
  var tempParams: immutable.Map[String, Any] = immutable.Map[String, Any]()
)

case class IndividualEvaluationPair(individual: ActorRef, evaluationAgent: ActorRef)
