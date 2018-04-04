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
    for (descriptor <- from) {
      minFitness = math.min(minFitness, descriptor.fitness)
      maxFitness = math.max(maxFitness, descriptor.fitness)
      size += 1
    }
    val randomLimit = rng.nextFloat()
    var reached = 0.0

    from find { descriptor =>
      reached += normalizer(descriptor.fitness, minFitness, maxFitness, size)
      reached >= randomLimit
    } getOrElse from.head
  }

}
