package com.obecto.operators
import com.obecto.genetics._

class InitializationGenerator(initializer: () => Chromosome) extends ChromosomeGenerator {

  def parentCount = 0
  def childCount = 1
  def weigth = 0.0f

  def apply(oldPopulation: Population): TraversableOnce[Chromosome] = {
    List(initializer())
  }
}
