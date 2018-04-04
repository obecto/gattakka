package com.obecto.gattakka

import akka.actor.{Actor, ActorRef, Props}
import com.obecto.gattakka.messages.evaluator._
import com.obecto.gattakka.messages.individual.{FitnessProducedEvent}
import com.obecto.gattakka.messages.population.{IntroducePopulation}

import scala.collection.mutable.HashMap

class Evaluator extends Actor {

  var populationActor = ActorRef.noSender
  val fitnesses = new HashMap[String, Double]()

  def customReceive: PartialFunction[Any, Unit] = PartialFunction.empty[Any, Unit]
  def originalReceive: PartialFunction[Any, Unit] = {
    case IntroducePopulation =>
      populationActor = sender()

    case RemoveFitness(id) =>
      fitnesses -= id

    case SetFitness(id, fitness) =>
      fitnesses(id) = fitness

    case FitnessProducedEvent(fitness) =>
      val id = sender.path.name
      self ! SetFitness(id, fitness)

    case GetFitness(id) =>
      sender() ! fitnesses(id)

    case GetAllFitnesses =>
      sender ! fitnesses.toMap
  }
  def receive: Receive = customReceive orElse originalReceive

}

object Evaluator {

  def props(customEvaluatorClass: Class[_]): Props = Props(customEvaluatorClass)
  def props(): Props = Props(classOf[Evaluator])

}
