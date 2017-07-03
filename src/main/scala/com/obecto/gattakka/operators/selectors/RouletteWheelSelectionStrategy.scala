package com.obecto.gattakka.operators
import com.obecto.gattakka.genetics._

class RouletteWheelSelectionStrategy(invert: Boolean = false, rng: scala.util.Random = scala.util.Random) extends SelectionStrategy {
  def apply(from: Population, count: Int): Seq[Chromosome] = {
    (0 until count).map((a: Int) => {
      val selected: Double = from.totalFitness * rng.nextFloat()
      var reached: Double = 0.0
      val result = from.chromosomes.find((chromosome) => {
        if (invert)
          reached = reached + (from.maxFitness - chromosome.calculatedFitness)
        else
          reached = reached + chromosome.calculatedFitness
        (reached >= selected)
      })
      result.getOrElse(from.chromosomes.last)
    })
  }
}
