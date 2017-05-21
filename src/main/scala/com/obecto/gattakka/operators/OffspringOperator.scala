package com.obecto.operators
import com.obecto.genetics._

class OffspringOperator(
    offspringStrategy: OffspringStrategy,
    selectionStrategy: SelectionStrategy,
    quotaPercentage: Float = 1.0f) extends Operator {

  def apply(oldGeneration: Generation, newGeneration: Generation) : Unit = {
    val childrenRequired = quotaPercentage * (newGeneration.targetPopulationSize - newGeneration.chromosomes.length)

    for (i <- 1 to Math.round(childrenRequired / offspringStrategy.childCount)) {
      val parents = selectionStrategy.apply(oldGeneration, offspringStrategy.parentCount)
      newGeneration.chromosomes ++= offspringStrategy.apply(parents)
    }

  }
}
