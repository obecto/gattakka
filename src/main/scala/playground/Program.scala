package playground

import akka.actor.ActorSystem
import com.obecto.gattakka._
import com.obecto.gattakka.genetics.operators.{BinaryMutationOperator, EliteOperator}
import com.obecto.gattakka.genetics.{Chromosome, Genome}
import com.obecto.gattakka.genetics.descriptors.{GeneGroupDescriptor, BigIntGeneDescriptor, GeneGroup, BigIntGene}
import com.obecto.gattakka.messages.individual.Initialize
import com.obecto.gattakka.messages.population.RefreshPopulation

import scala.language.postfixOps
import scala.concurrent.duration._


object Definitions {
  val chromosomeDescriptor = GeneGroupDescriptor(
    BigIntGeneDescriptor(8),
    BigIntGeneDescriptor(8)
  )
}

class CustomEvaluationAgent extends EvaluationAgent {

  override def onSignalReceived(data: Any): Unit = data match {
    case genome: Genome =>
      val chromosome = genome.chromosomes.head.toGene.asInstanceOf[GeneGroup]
      val x = chromosome.genes(0).asInstanceOf[BigIntGene].toDouble * 200 - 100
      val y = chromosome.genes(1).asInstanceOf[BigIntGene].toDouble * 200 - 100
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

  val pipelineOperators: List[PipelineOperator] = List(
    new EliteOperator {
      val elitePercentage = 0.2
    },
    new BinaryMutationOperator {
      val mutationChance = 0.02
    }
  )

  val pipelineActor = system.actorOf(Pipeline.props(pipelineOperators), "pipeline")
  val evaluator = system.actorOf(Evaluator.props(classOf[Evaluator],classOf[CustomEvaluationAgent]), "evaluator")
  val populationActor = system.actorOf(Population.props(
    classOf[CustomIndividualActor],
    initialChromosomes,
    evaluator,
    pipelineActor
  ), "population")


  system.scheduler.schedule(1 seconds, 100 milliseconds, populationActor, RefreshPopulation)


}
