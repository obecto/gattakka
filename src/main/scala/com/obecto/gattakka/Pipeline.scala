package com.obecto.gattakka

import akka.actor.{Actor, Props}
import com.obecto.gattakka.messages.population.RunPipeline

import scala.collection.immutable

class Pipeline(implicit val pipelineOperators: Seq[PipelineOperator]) extends Actor {

  def receive: Receive = {
    case RunPipeline(snapshot) =>
      sender ! runPipeline(snapshot)
  }

  def runPipeline(snapshot: List[IndividualDescriptor])(implicit pipelineOperators: Seq[PipelineOperator]): List[IndividualDescriptor] = {
    var processedSnapshot = snapshot
    pipelineOperators foreach {
      operator =>
        processedSnapshot = operator.apply(processedSnapshot)
    }
    processedSnapshot foreach {
      _.tempParams = immutable.Map[String, Any]()
    }
    processedSnapshot
  }

}

object Pipeline {

  def props(pipelineOperators: Seq[PipelineOperator]): Props = Props.apply(classOf[Pipeline], pipelineOperators)

}


trait PipelineOperator {

  def apply(snapshot: List[IndividualDescriptor]): List[IndividualDescriptor]

}
