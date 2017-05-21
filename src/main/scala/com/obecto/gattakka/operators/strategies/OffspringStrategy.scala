package com.obecto.operators
import com.obecto.genetics._

trait OffspringStrategy {
  def parentCount: Int
  def childCount: Int
  def apply(parents: Seq[Chromosome]): Seq[Chromosome]
}
