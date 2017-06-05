package com.obecto.gattakka.operators
import com.obecto.gattakka.genetics._

trait SelectionStrategy extends Serializable {
  def apply(from: Population, count: Int): Seq[Chromosome]
}
