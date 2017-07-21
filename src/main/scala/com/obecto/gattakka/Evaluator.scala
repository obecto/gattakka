package com.obecto.gattakka

import akka.actor.{Actor, ActorRef, Props, Terminated}
import com.obecto.gattakka.messages.evaluator.GetEvaluationAgent


 class Evaluator(environmentalData: AnyVal, population: ActorRef) extends Actor{

   def receive: Receive = {

     case GetEvaluationAgent =>
       sender() ! createEvaluationAgent()

     case Terminated(ref) =>
       println("My child died :( " + ref.path.name)

   }

   def createEvaluationAgent(): ActorRef ={
     val newEvaluationAgent = context.actorOf(Props(classOf[EvaluationAgent]))
   }
 }
