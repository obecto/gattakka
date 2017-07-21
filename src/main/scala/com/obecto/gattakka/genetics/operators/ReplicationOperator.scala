package com.obecto.gattakka.genetics.operators

import com.obecto.gattakka.genetics.Chromosome

/**
  * Created by gbarn_000 on 7/15/2017.
  */
trait ReplicationOperator {
  def replicate(parent1 : Chromosome, parent2 : Chromosome) : List[Chromosome]
}

object ReplicationOperator{
  trait SinglePointReplication extends ReplicationOperator{
    def replicate(parent1 : Chromosome, parent2 : Chromosome) : List[Chromosome] = {
      //assert(!parent1.equals(parent2))
      println("Children from: " + parent1.getClass + " and " + parent2.getClass)
      List(parent1,parent2)
    } //Implementation
  }
}

