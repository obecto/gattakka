package com.obecto.gattakka.operators
import com.obecto.gattakka.genetics._

class BinaryMutationGenerator(
    val weigth: Double = 1.0,
    selectionStrategy: SelectionStrategy,
    mutationChance: Float = 0.2f,
    rng: scala.util.Random = scala.util.Random) extends ChromosomeGenerator {
  def parentCount = 1
  def childCount = 1
  def apply(oldPopulation: Population): TraversableOnce[Chromosome] = {
    val parent: Chromosome = selectionStrategy.apply(oldPopulation, 1)(0)
    val genes: Seq[Gene[_]] = parent.genes.map(gene => {
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


    List(new Chromosome(genes))
  }
}
