package com.obecto.gattakka.operators
import com.obecto.gattakka.genetics._

class InitializationGenerator(initializer: () => Chromosome) extends ChromosomeGenerator {

  def parentCount = 0
  def childCount = 1

  def apply(oldPopulation: Population): TraversableOnce[Chromosome] = {
    List(initializer())
  }
}
