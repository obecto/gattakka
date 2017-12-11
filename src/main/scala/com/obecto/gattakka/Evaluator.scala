package com.obecto.gattakka

import akka.actor.{Actor, ActorRef, Props, Terminated}
import com.obecto.gattakka.messages.evaluation.{GetEvaluationAgent, SpawnEvaluationAgent}
import com.obecto.gattakka.messages.population.{IntroducePopulation, RefreshPopulation}

import scala.concurrent.duration._


class Evaluator(evaluationAgentType: Class[_ <: EvaluationAgent], environmentalData: Any) extends Actor {

  var populationActor: ActorRef = ActorRef.noSender

   def receive: Receive = customReceive orElse {

     case IntroducePopulation =>
       populationActor = sender()

     case GetEvaluationAgent(id: String) =>
       sender() ! tryGetEvaluationAgent(id)

     case SpawnEvaluationAgent(id: String) =>
       sender() ! createEvaluationAgent(id)

     case Terminated(ref) =>
     //println("My child died :( " + ref.path.name)

   }

  def customReceive: PartialFunction[Any, Unit] = PartialFunction.empty[Any, Unit]

  private def tryGetEvaluationAgent(id: String): Option[ActorRef] ={
    context.child(id)
  }

   def createEvaluationAgent(id: String): ActorRef ={
     val newEvaluationAgent = context.actorOf(Props(evaluationAgentType),id)
     context.watch(newEvaluationAgent)
     newEvaluationAgent
   }

 }

object Evaluator {

  def props(evaluationAgentType: Class[_ <: EvaluationAgent]): Props = Props(classOf[Evaluator], evaluationAgentType, "")
}
