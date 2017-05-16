package com.obecto.genetics

class Chromosome(var genes: Seq[Gene[_]] = Array[Gene[_]]()) {
  var calculatedFitness: Float = Float.NaN
}