package com.obecto.gattakka.genetics

import com.obecto.gattakka.genetics.descriptors.{GeneDescriptor, Gene}

case class Chromosome(byteArray: Array[Byte], descriptor: GeneDescriptor) {
  def value: Any = descriptor.apply(byteArray).value

  def toGene: Gene = descriptor.apply(byteArray)

  def diversity(chromosome: Chromosome): Double = {
    if (this.descriptor.getClass != chromosome.descriptor.getClass) {
      1
    } else {
      val geneTuples = this.byteArray.zip(chromosome.byteArray)

      val geneDiversitySum = geneTuples.map((geneTuple: (Byte, Byte)) => {
        val xorNum = (geneTuple._1 ^ geneTuple._2).toByte
        percentageOfBitsSet(xorNum)
      }).sum

      geneDiversitySum / geneTuples.length
    }
  }

  private def percentageOfBitsSet(num: Byte): Double = {
    val sumOfSetBits: Int = (0 to 7).map((i: Int) => (num >>> i) & 1).sum
    val percentageOfSetBits: Double = sumOfSetBits.toDouble / 8
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
