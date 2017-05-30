package com.obecto.operators
import com.obecto.genetics._

trait SelectionStrategy extends Serializable {
  def apply(from: Population, count: Int): Seq[Chromosome]
}
