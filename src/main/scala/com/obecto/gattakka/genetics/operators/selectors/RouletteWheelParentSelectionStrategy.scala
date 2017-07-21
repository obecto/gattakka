package com.obecto.gattakka.genetics.operators

import com.obecto.gattakka.genetics._

class RouletteWheelParentSelectionStrategy(rng: scala.util.Random = scala.util.Random) extends ParentSelectionStrategy {

  def select(from: Seq[Chromosome]): Chromosome = {
    val totalFitness = calculateTotalFitness(from)
    val randomLimit = totalFitness * rng.nextFloat()
    var reached = 0.0

    from find {
      chromosome =>
        reached += chromosome.fitness
        reached >= randomLimit
    } match {
      case Some(value) => value
      case None => throw new ArithmeticException("Unable to select parent due to problem in fitness calculation.")
    }
  }

  private def calculateTotalFitness(chromosomes: Seq[Chromosome]): Float = {
    var totalFitness = 0.0f
    chromosomes.foreach(totalFitness += _.fitness)
    totalFitness
  }

}
