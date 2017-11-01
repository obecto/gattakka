package com.obecto.gattakka.genetics.operators

import com.obecto.gattakka.PipelineOperator

trait ReplicationBaseOperator {
  self: PipelineOperator =>

  def parentSelectionStrategy: ParentSelectionStrategy
}
