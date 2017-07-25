package com.obecto.gattakka.genetics.operators

import com.obecto.gattakka.PipelineOperator

/**
  * Created by gbarn_000 on 7/25/2017.
  */
trait ReplicationBaseOperator {
  self: PipelineOperator =>
  def parentSelectionStrategy: ParentSelectionStrategy

}
