package com.obecto.operators
import com.obecto.genetics._

trait SelectionStrategy {
  def apply(from: Generation, count: Int): Seq[Chromosome]
}
