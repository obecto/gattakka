package com.obecto.operators
import com.obecto.genetics._

trait Operator {
  def apply(oldGeneration: Generation, newGeneration: Generation) : Unit
}
