package com.obecto.operators
import com.obecto.genetics._
import java.io.PrintStream

class PrintOperator(stream: PrintStream = Console.out) extends Operator {

  def apply(oldGeneration: Generation, newGeneration: Generation) : Unit = {
    val id = oldGeneration.sequentialId
    val maxFitness = oldGeneration.maxFitness
    val minFitness = oldGeneration.minFitness
    val population = oldGeneration.chromosomes.length
    val averageFitness = oldGeneration.totalFitness / oldGeneration.chromosomes.length
    Console.withOut(stream) {
      println(f"Generation: ${id}%-4d | Max F: ${maxFitness}% -6f | Min F: ${minFitness}% -6f | Avg F: ${averageFitness}% -6f | Population: ${population}%-7d")
    }
  }
}
