package com.obecto.operators
import com.obecto.genetics._

class RouletteWheelSelectionStrategy(rng: scala.util.Random = scala.util.Random) extends SelectionStrategy {
  def apply(from: Generation, count: Int): Seq[Chromosome] = {
    (0 until count).map((a: Int) => {
      val selected: Double = from.totalFitness * rng.nextFloat()
      var reached: Double = 0.0
      val result = from.chromosomes.find((chromosome) => {
        reached = reached + chromosome.calculatedFitness
        (reached >= selected)
      })
      result.getOrElse(from.chromosomes.last)
    })
  }
}
