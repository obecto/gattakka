package com.obecto.gattakka

import akka.actor.Actor
import com.obecto.gattakka.genetics.Genome
import com.obecto.gattakka.messages.individual._
import com.obecto.gattakka.messages.eventbus.{AddSubscriber}

abstract class Individual(chromosome: Genome) extends Actor {

  val lookupBusImpl = new LookupBusImplementation(self)

  def receive = customReceive orElse {

    case AddSubscriber(subscriber, classification) =>
      lookupBusImpl.subscribe(subscriber, classification)


    case x => println("Couldn't understand what to do with... "); print(x)
  }

  def customReceive: PartialFunction[Any, Unit] = PartialFunction.empty[Any, Unit]

  def dispatchFitness(fitness: Double): Unit = {
    lookupBusImpl.publish(FitnessProducedEvent(fitness))
  }
}
