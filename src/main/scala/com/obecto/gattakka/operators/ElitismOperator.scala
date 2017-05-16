package com.obecto.operators
import com.obecto.genetics._

class ElitismOperator(elitePercentage: Float = 0.05f, rng: scala.util.Random = scala.util.Random) extends Operator {
  def apply(oldGeneration: Generation, newGeneration: Generation) : Unit = {
    for(chromosome <- oldGeneration.chromosomes) {
      if (rng.nextFloat() < elitePercentage) {
        newGeneration.chromosomes += chromosome
      }
    }
  }
}
