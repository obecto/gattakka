package com.obecto.operators
import com.obecto.genetics._

trait ChromosomeGenerator extends Serializable {
  def weigth: Double
  def parentCount: Int
  def childCount: Int
  def apply(oldPopulation: Population) : TraversableOnce[Chromosome]
}
