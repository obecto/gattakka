package com.obecto.gattakka

import akka.actor.{Actor, Props}
import com.obecto.gattakka.messages.population.RunPipeline

class Pipeline(implicit val pipelineOperators: Seq[PipelineOperator]) extends Actor {

  var age = 0

  def receive: Receive = {
    case RunPipeline(snapshot) =>
      sender ! runPipeline(snapshot)
  }

  def runPipeline(snapshot: List[IndividualDescriptor])(implicit pipelineOperators: Seq[PipelineOperator]): List[IndividualDescriptor] = {
    var processedSnapshot = snapshot
    for (operator <- pipelineOperators) {
      processedSnapshot = operator.apply(processedSnapshot)
    }
    processedSnapshot
  }

}

object Pipeline {

  def props(pipelineOperators: Seq[PipelineOperator]): Props = Props.apply(classOf[Pipeline], pipelineOperators)

}

abstract class PipelineOperator {

  def apply(snapshot: List[IndividualDescriptor]): List[IndividualDescriptor]

}
