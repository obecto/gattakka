package com.obecto.gattakka.genetics.operators
import com.obecto.gattakka.genetics._

trait ParentSelectionStrategy {
  def select(from : Seq[Chromosome]): Chromosome
}
