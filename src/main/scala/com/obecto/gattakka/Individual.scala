package com.obecto.gattakka

import akka.actor.Actor
import com.obecto.gattakka.genetics.Genome
import com.obecto.gattakka.messages.eventbus.{AddSubscriber, HandleEvent}

abstract class Individual(chromosome: Genome) extends Actor {

  val lookupBusImpl = new LookupBusImplementation

  def receive = customReceive orElse {

    case AddSubscriber(subscriber, classification) =>
      lookupBusImpl.subscribe(subscriber, classification)


    case x => println("Couldn't understand what to do with... "); print(_)
  }

  def customReceive: PartialFunction[Any, Unit] = PartialFunction.empty[Any, Unit]

  //TODO remove hardcoded strings
  def dispatchEvent(payload: Any): Unit = {
    lookupBusImpl.publish(HandleEvent("individual_signal", payload))
  }
}
