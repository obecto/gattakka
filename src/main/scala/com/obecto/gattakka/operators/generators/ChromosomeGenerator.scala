package com.obecto.gattakka.operators
import com.obecto.gattakka.genetics._

trait ChromosomeGenerator extends Serializable {
  def parentCount: Int
  def childCount: Int
  def apply(oldPopulation: Population) : TraversableOnce[Chromosome]
}
