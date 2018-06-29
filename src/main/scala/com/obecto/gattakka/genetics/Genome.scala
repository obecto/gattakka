package com.obecto.gattakka.genetics

import scala.math.{max}

case class Genome(chromosomes: List[Chromosome]) {

  def diversity(genome: Genome): Double = {
    val chromosomeTuples = this.chromosomes.zip(genome.chromosomes)
    val chromosomeDiversitySum: Double = chromosomeTuples.map((chromosomeTuple: (Chromosome, Chromosome)) => {
      chromosomeTuple._1.diversity(chromosomeTuple._2)
    }).sum

    if (this.chromosomes.length != genome.chromosomes.length) {
      val maxLengthList = max(this.chromosomes.length, genome.chromosomes.length)
      val differentChromosomesCount = maxLengthList - chromosomeTuples.length
      (chromosomeDiversitySum + differentChromosomesCount * 100) / maxLengthList
    } else {
      chromosomeDiversitySum / chromosomeTuples.length
    }
  }
}
