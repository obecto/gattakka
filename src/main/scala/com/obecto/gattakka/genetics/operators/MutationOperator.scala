package com.obecto.gattakka.genetics.operators

import com.obecto.gattakka.genetics.Chromosome

/**
  * Created by gbarn_000 on 7/15/2017.
  */

trait MutationOperator{
  def mutationChance : Float
  def mutate(chromosome: Chromosome,mutationChance : Float) : Chromosome
}


object MutationOperator{
  trait BinaryMutation extends MutationOperator{
     val mutationChance: Float = 0.2f
     def mutate(chromosome: Chromosome, mutationChance: Float): Chromosome = {
       println("Mutation is real")
       chromosome
     }
  }
}

