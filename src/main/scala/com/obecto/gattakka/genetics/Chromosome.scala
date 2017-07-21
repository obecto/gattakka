package com.obecto.gattakka.genetics

import com.obecto.gattakka.genetics.operators.{MutationOperator, ReplicationOperator}

 class Chromosome(val genes: Seq[Gene[_]] = Array[Gene[_]]())  {

    var fitness = 0f

  def >< (mate : Chromosome)(implicit op: ReplicationOperator) : List[Chromosome] = {
    op.replicate(this,mate)
  }

  def @#! (implicit op: MutationOperator) : Chromosome = {
    op.mutate(this,op.mutationChance)
  }

/*  override def withGenes(newGenes: Seq[Gene[_]]): Chromosome = {
    assert(newGenes.size == genes.size)
    Chromosome(newGenes)
  }*/
 }
