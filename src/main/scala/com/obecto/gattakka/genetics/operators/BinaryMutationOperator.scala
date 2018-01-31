package com.obecto.gattakka.genetics.operators

import com.obecto.gattakka.genetics.{Chromosome}
import com.obecto.gattakka.{PipelineOperator}


trait BinaryMutationOperator extends PipelineOperator with ChromosomeMutationBaseOperator {

  def bitFlipChance: Double

  def apply(chromosome: Chromosome): Chromosome = {
    var chromosomeMutationOccured = false
    val mutatedByteArray: Array[Byte] = for (byte <- chromosome.byteArray) yield {
      var newByte = byte
      for (i <- 0 to 8) {
        if (rnd.nextFloat() < bitFlipChance) {
          chromosomeMutationOccured = true
          newByte = (newByte ^ (1 << i)).toByte
        }
      }
      newByte
    }

    if (chromosomeMutationOccured) {
      new Chromosome(mutatedByteArray, chromosome.descriptor)
    } else {
      chromosome
    }
  }
}
