package com.obecto.actors
import com.obecto.operators.{Pipeline}
import akka.actor.{Actor, Props}

class GeneticPopulationActor(evaluatorProps: Props, pipeline: Pipeline) extends Actor {
  val evaluator = context.system.actorOf(evaluatorProps, "evaluator")

  def receive = {
    case _ => println("Got a message")
  }
}