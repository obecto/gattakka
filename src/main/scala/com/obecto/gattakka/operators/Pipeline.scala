package com.obecto.gattakka.operators
import com.obecto.gattakka.genetics._
import scala.collection.{ TraversableOnce }

object Pipeline {
  lazy val empty = new Pipeline()
}

class Pipeline(val generators: List[ChromosomeGenerator] = List(), rng: scala.util.Random = scala.util.Random) extends Serializable {


  def withGenerator(generator: ChromosomeGenerator): Pipeline = new Pipeline(generators :+ generator)

  def apply(currentPopulation: Population, requiredAmount: Int): TraversableOnce[Chromosome] = {
    currentPopulation.recomputeValues()
    currentPopulation.resortChromosomes()

    val usableGenerators = generators.filter((generator) => generator.parentCount <= currentPopulation.chromosomes.size)
    val totalWeigth = usableGenerators.foldLeft(0.0)((current, generator) => current + generator.weigth)

    val selected: Double = totalWeigth * rng.nextDouble()
    var reached: Double = 0.0
    val selectedGenerator = usableGenerators.find((generator) => {
      reached = reached + generator.weigth
      (reached >= selected)
    }).getOrElse(usableGenerators.head)

    val builder = TraversableOnce.OnceCanBuildFrom[Chromosome]()
    var accumulatedSize = 0
    while (accumulatedSize < requiredAmount) {
      val generated = selectedGenerator.apply(currentPopulation)
      accumulatedSize += generated.size
      builder ++= generated
    }
    builder.result
  }
}
