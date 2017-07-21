package com.obecto.gattakka

import akka.actor.{Actor, ActorRef}
import akka.event.{EventBus, LookupClassification}
import com.obecto.gattakka.genetics.Chromosome

abstract class Individual(chromosome: Chromosome) extends Actor {

  import messages.individual._

  val lookupBusImpl = new LookupBusImpl

  def receive = customReceive orElse {

    case AddSubscriber(subscriber,classification) =>
      lookupBusImpl.subscribe(subscriber,classification)

    case _ => println("Couldn't understand what to do with..."); print(_)

  }

  def customReceive: PartialFunction[Any, Unit] = PartialFunction.empty[Any, Unit]

  def dispatchEvent(eventType: String, payload: Any): Unit ={
    lookupBusImpl.publish(HandleEvent(eventType,payload))
  }


  class LookupBusImpl extends EventBus with LookupClassification {
    type Event = HandleEvent
    type Classifier = String
    type Subscriber = ActorRef


    // is used for extracting the classifier from the incoming events
    override protected def classify(event: Event): Classifier = event.dataType

    // will be invoked for each event for all subscribers which registered themselves
    // for the event’s classifier
    override protected def publish(event: Event, subscriber: Subscriber): Unit = {
      subscriber ! event.payload
    }

    // must define a full order over the subscribers, expressed as expected from
    // `java.lang.Comparable.compare`
    override protected def compareSubscribers(a: Subscriber, b: Subscriber): Int =
    a.compareTo(b)

    // determines the initial size of the index data structure
    // used internally (i.e. the expected number of different classifiers)
    override protected def mapSize: Int = 128

  }
}

case class AddSubscriber(subscriber: ActorRef, classification: String)
case class HandleEvent(dataType: String, payload : Any)
case object PublishEvent
