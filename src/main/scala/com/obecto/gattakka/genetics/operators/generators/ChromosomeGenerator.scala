package com.obecto.gattakka.genetics.operators
import com.obecto.gattakka.genetics._

trait ChromosomeGenerator extends Serializable {
  def parentCount: Int
  def childCount: Int
  def apply(oldPopulation: EvaluationResult) : TraversableOnce[Chromosome]
}
