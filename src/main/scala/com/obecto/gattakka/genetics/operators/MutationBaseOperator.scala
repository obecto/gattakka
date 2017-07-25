package com.obecto.gattakka.genetics.operators

import com.obecto.gattakka.PipelineOperator

/**
  * Created by gbarn_000 on 7/19/2017.
  */
trait MutationBaseOperator {
  self: PipelineOperator =>
  val mutationChance: Float

}

