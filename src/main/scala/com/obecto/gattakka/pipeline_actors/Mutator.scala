package com.obecto.gattakka.pipeline_actors

import akka.actor.Actor
import com.obecto.gattakka.IndividualDescriptor
import com.obecto.gattakka.genetics.Chromosome
import com.obecto.gattakka.genetics.operators.MutationOperator

/**
  * Created by gbarn_000 on 7/19/2017.
  */
class Mutator(implicit val mutationOperator: MutationOperator) extends Actor{
   def receive: Receive = {
     case Mutate(descriptors) =>



   }

  def mutate(individualDescriptors: Iterable[IndividualDescriptor]): Iterable[IndividualDescriptor] ={
    individualDescriptors foreach {
      indDesc =>
        indDesc.chromosome @#!
    }
    individualDescriptors
  }


}

case class Mutate(individualDescriptors: Iterable[IndividualDescriptor])
