package com.obecto.gattakka.genetics

import scala.math.{abs}
import collection.mutable.{HashMap, HashSet}

case class Genome(chromosomes: List[Chromosome]) {

  def diversity(genome: Genome): Double = {
    var chromosomeTypes1 = HashMap[String, List[Chromosome]]()
    for (chromosome <- this.chromosomes) {
      val descriptor = chromosome.descriptor.getClass.toString
      if (chromosomeTypes1.contains(descriptor)) {
        chromosomeTypes1(descriptor) :+ chromosome
      } else {
        chromosomeTypes1(descriptor) = List[Chromosome](chromosome)
      }
    }


    var chromosomeTypes2 = HashMap[String, List[Chromosome]]()
    for (chromosome <- genome.chromosomes) {
      val descriptor = chromosome.descriptor.getClass.toString
      if (chromosomeTypes2.contains(descriptor)) {
        chromosomeTypes2(descriptor) :+ chromosome
      } else {
        chromosomeTypes2(descriptor) = List[Chromosome](chromosome)
      }
    }

    val commonKeysSet = chromosomeTypes1.keySet.intersect(chromosomeTypes2.keySet)
    val differentKeysSet = chromosomeTypes1.keySet diff chromosomeTypes2.keySet

    var diversity: Double = 0
    for (key <- commonKeysSet) {
      val res = calculateDiversityPerDescriptor(chromosomeTypes1(key), chromosomeTypes2(key))
      diversity += res
    }

    (diversity + differentKeysSet.size) / (commonKeysSet.size + differentKeysSet.size)
  }

  private def calculateDiversityPerDescriptor(l1: List[Chromosome], l2: List[Chromosome]): Double = {
    var diversity: Double = 0.0
    val size_difference = l1.size - l2.size
    if (size_difference == 0) {
      val chromosomeCombinations: List[(Chromosome, Chromosome)] = for (x <- l1; y <- l2) yield (x, y)
      val divSum: Double = (for (combination <- chromosomeCombinations) yield combination._1.diversity(combination._2)).sum
      diversity += (divSum / chromosomeCombinations.size)
    } else if (size_difference > 0) {
      val chromosomeCombinations: List[(Chromosome, Chromosome)] =
        for (x <- l1.take(l1.length - size_difference); y <- l2) yield (x, y)
      val z = for (combination <- chromosomeCombinations) yield combination._1.diversity(combination._2)
      val divSum: Double = z.sum
      diversity += (divSum + size_difference) / l1.size

    } else if (size_difference < 0) {
      val chromosomeCombinations: List[(Chromosome, Chromosome)] =
        for (x <- l2.take(l2.length - abs(size_difference)); y <- l1) yield (x, y)
      val divSum: Double = (for (combination <- chromosomeCombinations) yield combination._1.diversity(combination._2)).sum
      diversity += (divSum + abs(size_difference)) / l2.size
    }

    diversity
  }
}
