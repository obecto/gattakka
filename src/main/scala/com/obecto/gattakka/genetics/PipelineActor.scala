package com.obecto.gattakka.genetics

import akka.actor.{Actor, Props}
import com.obecto.gattakka.IndividualDescriptor

/**
  * Created by gbarn_000 on 7/20/2017.
  */
class PipelineActor(implicit val pipelineOperators: Seq[PipelineOperator]) extends Actor{

   def receive: Receive = {

     case RunPipeline(snapshot) =>
       runPipeline(snapshot)
  }

  def runPipeline(snapshot: List[IndividualDescriptor])(implicit pipelineOperators: Seq[PipelineOperator]): Unit ={
    var changedSnaphot = snapshot
    pipelineOperators foreach {
      operator =>
      changedSnaphot = operator.apply(snapshot)
    }
    changedSnaphot
  }

}

object PipelineActor{

  def props(pipelineOperators: Seq[PipelineOperator]): Props = Props.apply(classOf[PipelineActor],pipelineOperators)

}

case class RunPipeline(snapshot: List[IndividualDescriptor])

trait PipelineOperator{
  def apply(snapshot: List[IndividualDescriptor]): List[IndividualDescriptor]
}
