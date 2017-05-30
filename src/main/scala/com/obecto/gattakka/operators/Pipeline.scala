package com.obecto.operators
import com.obecto.genetics._
import scala.collection.{ TraversableOnce }

class Pipeline(val generators: List[ChromosomeGenerator] = List(), rng: scala.util.Random = scala.util.Random) extends Serializable {

  def withGenerator(generator: ChromosomeGenerator): Pipeline = new Pipeline(generators :+ generator)

  def apply(currentPopulation: Population, requiredAmount: Long): TraversableOnce[Chromosome] = {
    currentPopulation.recomputeValues()
    currentPopulation.resortChromosomes()

    val usableGenerators = generators.filter((generator) => generator.parentCount < currentPopulation.chromosomes.size)
    val totalWeigth = usableGenerators.foldLeft(0.0)((current, generator) => current + generator.weigth)

    val selected: Double = totalWeigth * rng.nextDouble()
    var reached: Double = 0.0
    val selectedGenerator = usableGenerators.find((generator) => {
      reached = reached + generator.weigth
      (reached >= selected)
    }).getOrElse(usableGenerators.head)

    val iterations: Long = Math.ceil(1.0 * requiredAmount / selectedGenerator.childCount).toLong
    val builder = TraversableOnce.OnceCanBuildFrom[Chromosome]()
    for (i <- 1l to iterations) {
      builder ++= selectedGenerator.apply(currentPopulation)
    }
    builder.result
  }
}