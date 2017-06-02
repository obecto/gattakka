package playground
import akka.actor.{ ActorSystem, Props, ActorRef }
// import scala.concurrent.duration._
import com.obecto.actors._
import com.obecto.operators._
import com.obecto.genetics._
import scala.concurrent.duration._
import scala.collection.mutable

case class FitnessResult(fitness: Float)

class CustomEvaluatorActor extends EvaluatorActor {
  import messages._

  val fitnesses = mutable.HashMap[ActorRef, Float]()

  override def customReceive = {
    case FitnessResult(fitness) =>
      fitnesses(context.sender) = fitness
      context.stop(context.sender)

    case GetEvaluatedFitnesses =>
      context.sender ! EvaluatedFitnessesResult(fitnesses.toMap)
  }
}

class CustomIndividualActor extends EvaluatorActor {
  import messages._

  val fitnesses = mutable.HashMap[ActorRef, Float]()

  override def customReceive = {
    case InitializeIndividual(chromosome, evaluator) =>
        val x = chromosome.genes(0).asInstanceOf[BinaryGene].toDouble * 200 - 100
        val y = chromosome.genes(1).asInstanceOf[BinaryGene].toDouble * 200 - 100
        val temp1 = Math.sin(Math.sqrt(x * x + y * y));
        val temp2 = 1 + 0.001 * (x * x + y * y);
        val result = 0.5 + (temp1 * temp1 - 0.5) / (temp2 * temp2);

        evaluator ! FitnessResult(result.toFloat)

  }
}

object RunGattakka extends App {
  val pipeline = new Pipeline(List(
    new InitializationGenerator(() => {
      new Chromosome(Array[Gene[_]](
        BinaryGene(32), BinaryGene(32)
      ))
    }),
    new BinaryMutationGenerator(1, new RouletteWheelSelectionStrategy(), 0.01f)
  ))

  // val chromosomes = (1 to 100).map((i: Int) => {
  //   new Chromosome(Array[Gene[_]](
  //     BinaryGene(32), BinaryGene(32)
  //   ))
  // })
  // var population = new Population(chromosomes.toArray)
  //
  // for (i <- 0 to 40) {
  //   for (chromosome <- population.chromosomes) {
  //     val x = chromosome.genes(0).asInstanceOf[BinaryGene].toDouble * 200 - 100
  //     val y = chromosome.genes(1).asInstanceOf[BinaryGene].toDouble * 200 - 100
  //     val temp1 = Math.sin(Math.sqrt(x * x + y * y));
  //     val temp2 = 1 + 0.001 * (x * x + y * y);
  //     val result = 0.5 + (temp1 * temp1 - 0.5) / (temp2 * temp2);
  //
  //     chromosome.calculatedFitness = (1 - result).toFloat;
  //   }
  //
  //   population.resortChromosomes()
  //   population.recomputeValues()
  //   pipeline.apply(population) // population =
  // }

  val system = ActorSystem("gattakka")

  val populationActor = system.actorOf(Props(classOf[GeneticPopulationActor],
      Props[CustomEvaluatorActor],
      Props[CustomIndividualActor],
      pipeline
    ), "population")

  populationActor ! messages.SetTargetPopulationSize(10)
  populationActor ! messages.StartGeneticAlgorithm

  import system.dispatcher

  // system.scheduler.scheduleOnce(2.seconds) {
  //   //system.terminate()
  //   populationActor ! messages.StopGeneticAlgorithm
  //   populationActor ! messages.SetTargetPopulationSize(0, true)
  // }

  //
}
