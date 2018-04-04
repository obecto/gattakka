package com.obecto.gattakka

import akka.actor.ActorRef
import akka.event.{EventBus, LookupClassification, ActorEventBus}

class LookupBusImplementation(sendAs: ActorRef) extends EventBus with LookupClassification with ActorEventBus {
  type Event = Any
  type Classifier = Class[_]

  override protected def classify(event: Event): Classifier = event.getClass()

  override protected def publish(event: Event, subscriber: Subscriber): Unit = {
    subscriber.tell(event, sendAs)
  }

  override protected def mapSize: Int = 16

}
