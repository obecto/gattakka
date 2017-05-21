package com.obecto.operators
import com.obecto.genetics._

class ElitismOperator(selectionStrategy: SelectionStrategy, elitePercentage: Float = 0.05f) extends Operator {
  def apply(oldGeneration: Generation, newGeneration: Generation) : Unit = {
    val count = Math.round(oldGeneration.chromosomes.length * elitePercentage)
    val selected = selectionStrategy.apply(oldGeneration, count)
    newGeneration.chromosomes ++= selected
  }
}
