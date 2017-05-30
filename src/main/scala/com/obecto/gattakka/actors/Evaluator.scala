package com.obecto.actors
import akka.actor.{Actor}

class EvaluatorActor extends Actor {
  def receive = {
    case _ => println("Got a message")
  }
}