package com.obecto.gattakka.genetics.operators

import com.obecto.gattakka.IndividualDescriptor

trait SelectionStrategy {
  def selectBest(from: Seq[IndividualDescriptor]): IndividualDescriptor
  def selectWorst(from: Seq[IndividualDescriptor]): IndividualDescriptor
}
