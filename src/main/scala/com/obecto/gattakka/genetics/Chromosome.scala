package com.obecto.gattakka.genetics

import scala.math.{max}
import com.obecto.gattakka.genetics.descriptors.{GeneDescriptor, Gene}

case class Chromosome(byteArray: Array[Byte], descriptor: GeneDescriptor) {
  def value: Any = descriptor.apply(byteArray).value

  def toGene: Gene = descriptor.apply(byteArray)

  def diversity(chromosome: Chromosome): Double = {
    if (this.byteArray.length == 0 && chromosome.byteArray.length == 0) {
      //they are same, because they are both empty
      100
    } else if (this.byteArray.length == 0 || chromosome.byteArray.length == 0) {
      //they are different, because one of the chromosomes is empty
      0
    } else {
      val geneTuples = this.byteArray.zip(chromosome.byteArray)

      val geneDiversitySum = geneTuples.map((geneTuple: (Byte, Byte)) => {
        val xorNum = (geneTuple._1 ^ geneTuple._2).toByte
        percentageOfBitsSet(xorNum)
      }).sum

      if (this.byteArray.length != chromosome.byteArray.length) {
        val maxLengthList = max(this.byteArray.length, chromosome.byteArray.length)
        val differentGenesCount = maxLengthList - geneTuples.length
        (geneDiversitySum + differentGenesCount * 100) / maxLengthList
      } else {
        geneDiversitySum / geneTuples.length
      }
    }
  }

  private def percentageOfBitsSet(num: Byte): Double = {
    val sumOfSetBits: Int = (0 to 7).map((i: Int) => (num >>> i) & 1).sum
    val percentageOfSetBits: Double = sumOfSetBits.toDouble / 8 * 100
    percentageOfSetBits
  }

  private def printGeneAsBits(gene: Byte): String = {
    var cutStr = gene.toBinaryString.takeRight(8)

    while (cutStr.length < 8) {
      cutStr = "0" + cutStr
    }

    cutStr
  }
}
