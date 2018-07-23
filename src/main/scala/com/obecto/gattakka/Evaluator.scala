package com.obecto.gattakka

import akka.actor.{Actor, ActorRef, Props}
import com.obecto.gattakka.messages.evaluator._
import com.obecto.gattakka.messages.individual.{FitnessProducedEvent, ProcessStartedEvent}
import com.obecto.gattakka.messages.population.IntroducePopulation

import scala.collection.mutable.HashMap

class Evaluator extends Actor {

  var populationActor = ActorRef.noSender
  val fitnesses = new HashMap[String, Double]()

  def customReceive: PartialFunction[Any, Unit] = PartialFunction.empty[Any, Unit]
  def originalReceive: PartialFunction[Any, Unit] = {
    case IntroducePopulation =>
      println("IntroducePopulation")
      populationActor = sender()

    case RemoveFitness(id) =>
      fitnesses -= id

    case SetFitness(id, fitness) =>
      fitnesses(id) = fitness

    case FitnessProducedEvent(fitness) =>
      val id = sender.path.name
      Evaluator.decrementCurrentProcessingIndividualsCount()
      self ! SetFitness(id, fitness)

    case GetFitness(id) =>
      sender() ! fitnesses(id)

    case GetAllFitnesses =>
      sender ! fitnesses.toMap

    case ProcessStartedEvent =>
      println(s"ProcessStartedEvent")
      Evaluator.incrementCurrentProcessingIndividualsCount()
  }
  def receive: Receive = customReceive orElse originalReceive

}

object Evaluator {
  var currentProcessingIndividualsCount: Int = 0

  def incrementCurrentProcessingIndividualsCount(): Unit = {
    currentProcessingIndividualsCount += 1
    println(s"processing Count: $currentProcessingIndividualsCount")
  }

  def decrementCurrentProcessingIndividualsCount(): Unit = {
    currentProcessingIndividualsCount -= 1
    println(s"processing Count: $currentProcessingIndividualsCount")
  }

  def props(customEvaluatorClass: Class[_]): Props = Props(customEvaluatorClass)
  def props(): Props = Props(classOf[Evaluator])

}
