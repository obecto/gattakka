package com.obecto.gattakka.genetics.operators

import com.obecto.gattakka.PipelineOperator

trait MutationBaseOperator {
  self: PipelineOperator =>

  def mutationChance: Double
}
