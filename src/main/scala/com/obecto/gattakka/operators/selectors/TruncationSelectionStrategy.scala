package com.obecto.gattakka.operators
import com.obecto.gattakka.genetics._

class TruncationSelectionStrategy(invert: Boolean = false) extends SelectionStrategy {
  def apply(from: Population, count: Int): Seq[Chromosome] = {
    if (invert)
      from.chromosomes.slice(from.chromosomes.size - count, from.chromosomes.size)
    else
      from.chromosomes.slice(0, count)
  }
}
