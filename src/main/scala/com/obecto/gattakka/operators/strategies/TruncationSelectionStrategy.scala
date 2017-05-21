package com.obecto.operators
import com.obecto.genetics._

class TruncationSelectionStrategy() extends SelectionStrategy {
  def apply(from: Generation, count: Int): Seq[Chromosome] = {
    from.chromosomes.slice(0, count)
  }
}
