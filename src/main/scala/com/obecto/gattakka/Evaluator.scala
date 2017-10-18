package com.obecto.gattakka

import akka.actor.{Actor, ActorRef, Props, Terminated}
import com.obecto.gattakka.messages.evaluation.GetEvaluationAgent
import com.obecto.gattakka.messages.population.RefreshPopulation
import scala.concurrent.duration._


class Evaluator(evaluationAgentType: Class[_ <: EvaluationAgent], environmentalData: Any) extends Actor {

   def receive: Receive = {

     case GetEvaluationAgent =>
       val evaluationAgent = createEvaluationAgent()
       sender() ! evaluationAgent

     case Terminated(ref) =>
     //println("My child died :( " + ref.path.name)

   }

   def createEvaluationAgent(): ActorRef ={
     val newEvaluationAgent = context.actorOf(Props(evaluationAgentType))
     context.watch(newEvaluationAgent)
     newEvaluationAgent
   }

 }

object Evaluator {

  def props(evaluationAgentType: Class[_ <: EvaluationAgent]): Props = Props(classOf[Evaluator], evaluationAgentType, "")
}
