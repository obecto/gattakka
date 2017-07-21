package com.obecto.gattakka

import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.Patterns
import akka.util.Timeout
import com.obecto.gattakka.genetics.Chromosome
import com.obecto.gattakka.genetics.operators.{MutationOperator, ParentSelectionStrategy, ReplicationOperator}
import com.obecto.gattakka.messages.evaluator.GetEvaluationAgent
import com.obecto.gattakka.messages.individual.Initialize
import com.obecto.gattakka.messages.population.{ReceiveSignal, RefreshPopulation}

import scala.concurrent.Await

object GeneticPopulation {
  def props(individualActorType: Class[_ <: Individual],
            biologicalOperators: BiologicalOperators,
            evaluator: ActorRef,
            initialChromosomes: Seq[Chromosome],
            environmentalData: AnyVal,
            populationSize: Int = 100
           ): Props = {
    Props(classOf[GeneticPopulation], individualActorType, biologicalOperators, evaluator,
      initialChromosomes, environmentalData, populationSize)
  }
}

class GeneticPopulation(individualActorType: Class[_ <: Individual],
                        biologicalOperators: BiologicalOperators,
                        evaluator: ActorRef,
                        initialChromosomes: Seq[Chromosome],
                        environmentalData: AnyVal,
                        populationSize: Int = 100
                       ) extends Actor {


  implicit val timeout = Config.REQUEST_TIMEOUT
  /* implicit val repOp = biologicalOperators.replicator.getOrElse(new SinglePointReplication {})
   implicit val mutOp = biologicalOperators.mutator.getOrElse(new BinaryMutation {})
   implicit val parentSelectionStrategy = biologicalOperators.parentSelectionStrategy.getOrElse(
     new RouletteWheelParentSelectionStrategy()
   )*/
  //val retainParentsPercentage = 0.1f




  val firstIndividualDescriptors: Seq[IndividualDescriptor] = initialChromosomes map {
    chromosome =>
      IndividualDescriptor(None, chromosome)
  }

  var currentIndividualDescriptors: List[IndividualDescriptor] = hatchPopulation(firstIndividualDescriptors,evaluator)

  def receive: Receive = {

    /*case ReceiveSignal(data) =>
      currentIndividuals.find(individualDescriptor => {
        var isEqual = false
        if (individualDescriptor.individual.nonEmpty) {
          isEqual = individualDescriptor.individual.get.equals(sender())
        }
        isEqual
      }) match {
        case Some(descriptor) => descriptor.evaluator.handleSignal(data)
        case None =>
      }*/

    case RefreshPopulation =>
      handleRefreshPipelineRequest()

  }

  def handleRefreshPipelineRequest(): Unit = {}

  def runPipeline(): Unit = {}


  /*def reproduceChromosomes(chromosomes: Seq[Chromosome])
                          (implicit parentSelectionStrategy: ParentSelectionStrategy): List[Chromosome] = {

    println("Reproducing chromosomes")
    val children: ListBuffer[Chromosome] = ListBuffer.empty
    do {
      val parent1 = parentSelectionStrategy.select(chromosomes)
      val parent2 = parentSelectionStrategy.select(chromosomes.filter(!_.equals(parent1)))
      children.++=:(parent1 >< parent2).toList
    } while (children.length <= chromosomes.length)

    children.toList.map(_ @#!).slice(0, chromosomes.length)
  }*/

  def hatchPopulation(descriptors: Iterable[IndividualDescriptor], evaluator: ActorRef)(implicit timeout: Timeout): List[IndividualDescriptor] = {
    descriptors foreach {
      descriptor =>
        val individual = giveBirthToIndividual(descriptor.chromosome)
        val evaluationAgent = getEvaluationAgent(individual,evaluator)
        initializeNewbornIndividual(individual)
        descriptor.individualEvaluationPair = Some(IndividualEvaluationPair(individual,evaluationAgent))
    }
    descriptors.toList
  }

  def giveBirthToIndividual(chromosome: Chromosome): ActorRef = {
    val individual = context.actorOf(Props.apply(individualActorType, chromosome))
    context.watch(individual)
    individual
  }

  def getEvaluationAgent(individual: ActorRef, evaluator: ActorRef)(implicit timeout: Timeout): ActorRef ={
    try{
      val evaluationAgentFuture  = Patterns.ask(evaluator,GetEvaluationAgent,timeout)
      val evaluationAgent = Await.result(evaluationAgentFuture,timeout.duration).asInstanceOf[ActorRef]
      evaluationAgent
    } catch {
      case exc : Exception => throw exc
    }
  }

  def initializeNewbornIndividual(individual: ActorRef): ActorRef = {
    individual ! Initialize(environmentalData)
    individual
  }
}


case class IndividualDescriptor(var individualEvaluationPair: Option[IndividualEvaluationPair], chromosome: Chromosome)
case class IndividualEvaluationPair(individual: ActorRef, evaluationAgent: ActorRef)

case class BiologicalOperators(parentSelectionStrategy: Option[ParentSelectionStrategy] = None,
                               replicator: Option[ReplicationOperator] = None,
                               mutator: Option[MutationOperator] = None)
