package com.obecto.operators
import com.obecto.genetics._

class Pipeline(var operators: List[Operator] = List(), var targetPopulationSize: Int = 20) {
  def addOperator(operator: Operator): Unit = {
    operators = operators :+ operator
  }

  def apply(oldGeneration: Generation): Generation = {
    oldGeneration.recomputeValues()
    oldGeneration.resortChromosomes()
    val newGeneration = new Generation(oldGeneration.sequentialId + 1, targetPopulationSize)
    for (operator <- operators) {
      operator.apply(oldGeneration, newGeneration)
    }
    newGeneration
  }
}