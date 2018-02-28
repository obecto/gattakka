package com.obecto.gattakka.genetics.descriptors

import com.obecto.gattakka.genetics.Chromosome
import scala.util.Random

trait GeneDescriptor {
  /** Length of the produced genes in **bits**. Usually a multiple of 8. */
  def length: Int

  /** Length of the produced genes in whole **bytes**. Computed automatically from length. */
  lazy val byteLength: Int = (length.toDouble / 8).ceil.toInt

  /** Returns a newly-generated gene according to the descriptor's specification. */
  def apply(rnd: Random = Random): Gene

  /** Returns a new gene using the supplied byte array. */
  def apply(bytes: Array[Byte]): Gene

  /** Helper for generating chromosomes from this [[GeneDescriptor]]. */
  def createChromosome(rnd: Random = Random): Chromosome = {
    val gene = apply()
    new Chromosome(gene.toByteArray, this)
  }
}
