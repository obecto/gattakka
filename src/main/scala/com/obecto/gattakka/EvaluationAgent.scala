package com.obecto.gattakka

import akka.actor.{Actor, ActorRef}
import akka.event.{EventBus, LookupClassification, SubchannelClassification}
import akka.util.Subclassification

/**
  * Created by gbarn_000 on 7/19/2017.
  */
class EvaluationAgent extends Actor{
  var fitness = 0.0f

  def receive: Receive = {

    case data: Any =>
      onDataReceived(data)

  }

  def onDataReceived(data: Any): Unit = data match {
    case data: Float =>
      fitness = data
  }
}
case class HandleEvent(dataType: String, payload : Any)

final case class MsgEnvelope(topic: String, payload: Any)

