package com.obecto.actors
import akka.actor.{Actor}

class IndividualActor extends Actor {
  def receive = {
    case _ => println("Got a message")
  }
}