package com.obecto.gattakka.genetics.operators
import com.obecto.gattakka.genetics._

class TruncationParentSelectionStrategy(invert: Boolean = false) extends ParentSelectionStrategy {
  def apply(from: EvaluationResult, count: Int): Seq[Chromosome] = {
    if (invert)
      from.chromosomes.slice(from.chromosomes.size - count, from.chromosomes.size)
    else
      from.chromosomes.slice(0, count)
  }

  //override def select(from: Seq[ChromosomeBase]): ChromosomeBase = ???
  override def select(from: Seq[Chromosome]): Chromosome = ???
}
