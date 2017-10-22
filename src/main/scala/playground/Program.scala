package playground

import akka.actor.ActorSystem
import com.obecto.gattakka._
import com.obecto.gattakka.genetics.operators.{BinaryMutationOperator, EliteOperator}
import com.obecto.gattakka.genetics.{Chromosome, Genome, IntegerGene}
import com.obecto.gattakka.messages.individual.Initialize
import com.obecto.gattakka.messages.population.RefreshPopulation

import scala.concurrent.duration._


class CustomEvaluationAgent extends EvaluationAgent {

  override def onSignalReceived(data: Any): Unit = data match {
    case genome: Genome =>
      val x = genome.chromosomes.head.genes.head.asInstanceOf[IntegerGene].toDouble * 200 - 100
      val y = genome.chromosomes.head.genes(1).asInstanceOf[IntegerGene].toDouble * 200 - 100
      val temp1 = Math.sin(Math.sqrt(x * x + y * y))
      val temp2 = 1 + 0.001 * (x * x + y * y)
      fitness = (0.5 + (temp1 * temp1 - 0.5) / (temp2 * temp2)).toFloat
    //println(s"Fitness is: $fitness")
  }
}

class CustomIndividualActor(genome: Genome) extends Individual(genome) {

  override def customReceive = {
    case Initialize(data) =>
      dispatchEvent(genome)
      //TODO Extract data from chromosomes and connect with TA-LIB
  }

  def setSignal(when : Int): Unit ={
    //TODO Set signal Buy/Sell at ...
    //at the given moment ->
    // population ! Buy/Sell Event
  }
}

object RunGattakka extends App {

  val initialChromosomes = (1 to 50).map((i: Int) => {
    new Genome(List(new Chromosome))
  }).toList

  val system = ActorSystem("gattakka")

  import system.dispatcher

  val pipelineOperators: List[PipelineOperator] = List(new EliteOperator {}, new BinaryMutationOperator {
    override val mutationChance: Float = 0.002f
  })
  val pipelineActor = system.actorOf(Pipeline.props(pipelineOperators))
  val evaluator = system.actorOf(Evaluator.props(classOf[CustomEvaluationAgent]), "evaluator")

  val populationActor = system.actorOf(Population.props(
    classOf[CustomIndividualActor],
    initialChromosomes,
    evaluator,
    pipelineActor
  ), "population")


  system.scheduler.schedule(1 seconds, 100 milliseconds, populationActor, RefreshPopulation)


}
