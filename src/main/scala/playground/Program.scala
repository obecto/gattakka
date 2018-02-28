package playground

import akka.actor.ActorSystem
import com.obecto.gattakka._
import com.obecto.gattakka.genetics.operators
import com.obecto.gattakka.genetics.{Genome}
import com.obecto.gattakka.genetics.descriptors.{MapGeneGroupDescriptor, DoubleGeneDescriptor}
import com.obecto.gattakka.messages.individual.Initialize
import com.obecto.gattakka.messages.population.RefreshPopulation

import scala.language.postfixOps
import scala.concurrent.duration._


object Definitions {
  val chromosomeDescriptor = MapGeneGroupDescriptor(
    "x" -> DoubleGeneDescriptor(-100, 100),
    "y" -> DoubleGeneDescriptor(-100, 100),
  )
}

class CustomEvaluationAgent extends EvaluationAgent {

  override def onSignalReceived(data: Any): Unit = data match {
    case genome: Genome =>
      val values = genome.chromosomes.head.toGene.value.asInstanceOf[Map[String, Double]]
      val x = values("x")
      val y = values("y")
      val temp1 = Math.sin(Math.sqrt(x * x + y * y))
      val temp2 = 1 + 0.001 * (x * x + y * y)
      fitness = (0.5 + (temp1 * temp1 - 0.5) / (temp2 * temp2)).toDouble
  }
}

class CustomIndividualActor(genome: Genome) extends Individual(genome) {

  override def customReceive = {
    case Initialize(data) =>
      dispatchEvent(genome)
  }
}

object RunGattakka extends App {

  val initialChromosomes = (1 to 50).map((i: Int) => {
    new Genome(List(
      Definitions.chromosomeDescriptor.createChromosome()
    ))
  }).toList

  val system = ActorSystem("gattakka")

  import system.dispatcher

  import operators._
  val pipelineOperators: List[PipelineOperator] = List(
    new EliteOperator {
      val elitePercentage = 0.1
    },
    new UniformCrossoverReplicationOperator {
      val replicationChance = 0.5
      override val keepFirstChildOnly = true
      // val parentSelectionStrategy = new RouletteWheelSelectionStrategy()
      val parentSelectionStrategy = new TournamentSelectionStrategy(4)
      // val parentSelectionStrategy = new TournamentSelectionStrategy(1)
    },
    new BinaryMutationOperator {
      val mutationChance = 0.2
      val bitFlipChance = 2.0 / 16
    },
    new DeduplicationOperator {},
    new LimitSizeOperator {
      val targetPopulationSize = 50
    },
  )

  val pipelineActor = system.actorOf(Pipeline.props(pipelineOperators), "pipeline")
  val evaluator = system.actorOf(Evaluator.props(classOf[CustomEvaluationAgent]), "evaluator")
  val populationActor = system.actorOf(Population.props(
    classOf[CustomIndividualActor],
    initialChromosomes,
    evaluator,
    pipelineActor
  ), "population")


  system.scheduler.schedule(1 seconds, 80 milliseconds, populationActor, RefreshPopulation(false))
  system.scheduler.scheduleOnce((1 seconds) + (80 milliseconds) * 100) {
    system.terminate()
  }


}
