package com.obecto.gattakka.genetics.operators

import com.obecto.gattakka.IndividualDescriptor

trait ParentSelectionStrategy {
  def select(from: Seq[IndividualDescriptor]): IndividualDescriptor
}
