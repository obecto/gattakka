package com.obecto.operators
import com.obecto.genetics._

class BinaryMutationStrategy(mutationChance: Float = 0.2f, rng: scala.util.Random = scala.util.Random) extends OffspringStrategy {
  def parentCount = 1
  def childCount = 1
  def apply(parents: Seq[Chromosome]): Seq[Chromosome] = {
    val genes: Seq[Gene[_]] = parents(0).genes.map(gene => {
      val bytes: Array[Byte] = gene.toByteArray

      val newBytes = bytes.map(byte => {
        var newByte = byte
        for (i <- 0 to 8) {
          if (rng.nextFloat() < mutationChance / 2) {
            newByte = (newByte ^ (1 << i)).toByte
          }
        }

        newByte
      })

      gene.fromByteArray(newBytes)
    })

    // println(s"Old: ${parents(0).genes(0).toByteArray(0).toBinaryString}, New: ${genes(0).toByteArray(0).toBinaryString}")
    // assert(false)

    val offspring = new Chromosome(genes)
    List(offspring)
  }
}
