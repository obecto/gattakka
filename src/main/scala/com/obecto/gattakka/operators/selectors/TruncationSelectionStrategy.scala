package com.obecto.gattakka.operators
import com.obecto.gattakka.genetics._

class TruncationSelectionStrategy() extends SelectionStrategy {
  def apply(from: Population, count: Int): Seq[Chromosome] = {
    from.chromosomes.slice(0, count)
  }
}
