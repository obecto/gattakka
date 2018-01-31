package com.obecto.gattakka.genetics.operators

import com.obecto.gattakka.IndividualDescriptor

class RouletteWheelSelectionStrategy(rng: scala.util.Random = scala.util.Random) extends SelectionStrategy {

  def selectBest(from: Seq[IndividualDescriptor]): IndividualDescriptor = {
    select(from) { (fitness, min, max, n) =>
      (fitness - min) / (max - min) / n
    }
  }

  def selectWorst(from: Seq[IndividualDescriptor]): IndividualDescriptor = {
    select(from) { (fitness, min, max, n) =>
      (1.0 - (fitness - min) / (max - min)) / n
    }
  }

  def select(from: Seq[IndividualDescriptor])(normalizer: (Double, Double, Double, Int) => Double): IndividualDescriptor = {
    var minFitness = Double.MaxValue
    var maxFitness = Double.MinValue
    var size = 0
    for (desc <- from) {
      minFitness = math.min(minFitness, desc.currentFitness)
      maxFitness = math.max(maxFitness, desc.currentFitness)
      size += 1
    }
    val randomLimit = rng.nextFloat()
    var reached = 0.0

    from find { desc =>
      reached += normalizer(desc.currentFitness, minFitness, maxFitness, size)
      reached >= randomLimit
    } getOrElse from.head
  }

}
