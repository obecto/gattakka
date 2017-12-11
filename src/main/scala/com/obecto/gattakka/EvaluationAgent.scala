package com.obecto.gattakka

import akka.actor.Actor
import com.obecto.gattakka.messages.evaluation.GetFitness

/**
  * Created by gbarn_000 on 7/19/2017.
  */
abstract class EvaluationAgent extends Actor {
  var fitness: Double = 0.0

  def receive: Receive = {

    case GetFitness =>
      sender() ! fitness

    case data: Any =>
      onSignalReceived(data)
  }

  def onSignalReceived(data: Any): Unit = data match {
    //TODO override function to set fitness
    case data: Double =>
      fitness = data
  }

  /*  def subscribeTo(actor: ActorRef, classification: String): Unit ={
      actor ! AddSubscriber(self, classification)
    }*/

}

