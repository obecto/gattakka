package com.obecto.gattakka.genetics.operators

import com.obecto.gattakka.{IndividualDescriptor, PipelineOperator}

trait SinglePointReplicationOperator extends ReplicationBaseOperator with PipelineOperator {

  def apply(snapshot: List[IndividualDescriptor]): List[IndividualDescriptor] = {
    val parent1 = parentSelectionStrategy.select(snapshot)
    val parent2 = parentSelectionStrategy.select(snapshot.filter(!_.equals(parent1)))
    //TODO Implement single point replication
    snapshot
  }

}


//children.++=:(parent1 >< parent2).toList
