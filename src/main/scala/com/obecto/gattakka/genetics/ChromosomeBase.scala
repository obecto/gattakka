package com.obecto.gattakka.genetics

trait ChromosomeBase[+Repr] {
  def genes: Seq[Gene[_]]

  var calculatedFitness: Float = Float.NaN

  def getMutationRate(): Float = 1f

  def withFitness(newFitness: Float): Repr
  def withGenes(newGenes: Seq[Gene[_]]): Repr
}
