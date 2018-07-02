package playground

import akka.actor.{Actor, ActorRef, ActorSystem}
import com.obecto.gattakka._
import com.obecto.gattakka.genetics.operators
import com.obecto.gattakka.genetics.{Genome}
import com.obecto.gattakka.genetics.descriptors._
import com.obecto.gattakka.messages.eventbus.AddSubscriber
import com.obecto.gattakka.messages.individual.Initialize
import com.obecto.gattakka.messages.population.{IntroducePopulation, RefreshPopulation, PipelineFinishedEvent}

import scala.language.postfixOps
import scala.concurrent.duration._


object Definitions {
  val chromosomeDescriptor = MapGeneGroupDescriptor(
    "x" -> DoubleGeneDescriptor(-100, 100),
    "y" -> DoubleGeneDescriptor(-100, 100),
  )
}

class CustomIndividualActor(genome: Genome) extends Individual(genome) {

  import context.dispatcher

  override def customReceive = {
    case Initialize(data) =>
      val values = genome.chromosomes.head.toGene.value.asInstanceOf[Map[String, Double]]
      val x = values("x")
      val y = values("y")
      val temp1 = Math.sin(Math.sqrt(x * x + y * y))
      val temp2 = 1 + 0.001 * (x * x + y * y)
      val fitness = (0.5 + (temp1 * temp1 - 0.5) / (temp2 * temp2)).toDouble
      context.system.scheduler.scheduleOnce((scala.util.Random.nextInt(1000)) milliseconds) {
        dispatchFitness(fitness)
      }

  }
}

class CustomEvaluator extends Evaluator {

  import com.obecto.gattakka.messages.evaluator._

  var populationSize = 0
  val requiredRatio = 0.9

  def tryRefresh() = {
    if (fitnesses.size >= populationSize * requiredRatio && populationSize > 0) {
      populationActor ! RefreshPopulation(false)
    }
  }

  override def customReceive = {
    case IntroducePopulation =>
      originalReceive(IntroducePopulation)
      populationActor ! AddSubscriber(self, classOf[PipelineFinishedEvent])

    case PipelineFinishedEvent(totalSize, newComers) =>
      populationSize = totalSize
      tryRefresh()

    case x @ (_: SetFitness | _: RemoveFitness) =>
      originalReceive(x)
      tryRefresh()
  }
}

object RunGattakka extends App {

  val initialChromosomes = (1 to 50).map((i: Int) => {
    new Genome(List(
      Definitions.chromosomeDescriptor.createChromosome()
    ))
  }).toList

  // val t = EnumGeneDescriptor(-10, 10)

  // println((1 to 500).view.map((i: Int) => { t().value }).groupBy(x => x).map(_._2.size))

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
    new DiversitySelectionOperator {
      val targetPopulationSize = 50
    },
  )

  val pipelineActor = system.actorOf(Pipeline.props(pipelineOperators), "pipeline")
  val evaluator = system.actorOf(Evaluator.props(classOf[CustomEvaluator]), "evaluator")
  val populationActor = system.actorOf(Population.props(
    classOf[CustomIndividualActor],
    initialChromosomes,
    evaluator,
    pipelineActor
  ), "population")

  populationActor ! RefreshPopulation()


  // system.scheduler.schedule(1 seconds, 100 milliseconds, populationActor, RefreshPopulation(false))
  system.scheduler.scheduleOnce((1 seconds) + (100 milliseconds) * 100) {
    system.terminate()
  }

}
