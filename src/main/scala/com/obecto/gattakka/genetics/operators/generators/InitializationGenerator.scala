package com.obecto.gattakka.genetics.operators
import com.obecto.gattakka.genetics._

class InitializationGenerator(initializer: () => Chromosome) extends ChromosomeGenerator {

  def parentCount = 0
  def childCount = 1

  def apply(oldPopulation: EvaluationResult): TraversableOnce[Chromosome] = {
    List(initializer())
  }
}
