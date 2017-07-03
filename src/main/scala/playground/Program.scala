package playground
import akka.actor.{ ActorSystem, Props, ActorRef, Terminated }
import akka.pattern.{ ask }
import akka.util.{ Timeout }
// import scala.concurrent.duration._
import com.obecto.gattakka.actors._
import com.obecto.gattakka.operators._
import com.obecto.gattakka.genetics._
import scala.concurrent.duration._
import scala.collection.mutable
import scala.util.Success

case class FitnessResult(chromosome: Chromosome, fitness: Float)

class CustomEvaluatorActor extends EvaluatorActor {
  import messages.evaluator._

  val chromosomes = mutable.HashMap[ActorRef, Chromosome]()
  val fitnesses = mutable.HashMap[Chromosome, Float]()

  override def customReceive = {
    case IntroduceIndividual(chromosome, actor) =>
      chromosomes(actor) = chromosome
      context.watch(actor)

    case FitnessResult(chromosome, fitness) =>
      fitnesses(chromosome) = fitness

    case Terminated(actor) =>
      fitnesses.remove(chromosomes(actor))
      chromosomes.remove(actor)

    case GetEvaluatedPopulation =>
      context.sender ! EvaluatedPopulationResult(Population.from(fitnesses.toMap))
  }
}

class CustomIndividualActor extends IndividualActor {
  import messages.individual._

  override def customReceive = {
    case Initialize(chromosome, evaluator) =>
      val x = chromosome.genes(0).asInstanceOf[BinaryGene].toDouble * 200 - 100
      val y = chromosome.genes(1).asInstanceOf[BinaryGene].toDouble * 200 - 100
      val temp1 = Math.sin(Math.sqrt(x * x + y * y));
      val temp2 = 1 + 0.001 * (x * x + y * y);
      val result = 0.5 + (temp1 * temp1 - 0.5) / (temp2 * temp2);

      evaluator ! FitnessResult(chromosome, result.toFloat)

  }
}

object RunGattakka extends App {

  val initialChromosomes = (1 to 100).map((i: Int) => {
    new VariableMutationChromosome(BinaryGene(2), Array[Gene[_]](
      BinaryGene(32), BinaryGene(32)
    ))
  })

  val system = ActorSystem("gattakka")

  val populationActor = system.actorOf(GeneticPopulationActor.getProps(
      Props[CustomEvaluatorActor],
      Props[CustomIndividualActor]
    ), "population")

  populationActor ! messages.population.CreateIndividuals(initialChromosomes)
  val creator = system.actorSelection(populationActor.path.child("creator"))
  val destructor = system.actorSelection(populationActor.path.child("destructor"))

  creator ! messages.creator.AddGenerator(new BinaryMutationGenerator(new RouletteWheelSelectionStrategy(), 0.01f))

  creator ! messages.creator.SetTargetPopulationSize(1000)
  destructor ! messages.destructor.SetParameters(new RouletteWheelSelectionStrategy(true), 0.25f)
  // destructor ! messages.destructor.SetParameters(new TruncationSelectionStrategy(true), 0.25f)

  import system.dispatcher

  destructor.resolveOne(1.seconds) andThen {
    case Success(resolvedDestructor) =>
      system.scheduler.schedule(0.seconds, 0.14234234.seconds, resolvedDestructor, messages.destructor.KillIndividuals);
  }


  val start = compat.Platform.currentTime
  var averageMutationRate = Float.NaN

  // System.err.println("T, Max Fitness, Min Fitness, Average Fitness, Average mutation rate, Population size")

  system.scheduler.schedule(0.seconds, 0.1.seconds) {
    implicit val timeout = Timeout(1.second)

    populationActor ask messages.population.GetStatistics andThen {
      case Success(messages.population.StatisticsResult(statistics)) =>
        val passed = (compat.Platform.currentTime - start) // 1000d
        // System.err.println(s"${passed.toInt}, ${statistics.maxFitness}, ${statistics.minFitness}, ${statistics.averageFitness}, ${averageMutationRate}, ${statistics.populationSize}")
        println(f"T: ${passed.toInt}%4d:${(passed % 1f * 60).toInt}%-2d | Max F: ${statistics.maxFitness}% -6f | Min F: ${statistics.minFitness}% -6f | Avg F: ${statistics.averageFitness}% -6f | Avg Mut: ${averageMutationRate}%2f | Population: ${statistics.populationSize}%-7d")
    }
  }

  system.scheduler.schedule(0.seconds, 0.5.seconds) {
    implicit val timeout = Timeout(1.second)

    populationActor ask messages.population.GetPopulation andThen {
      case Success(messages.population.PopulationResult(population)) =>
        val mutationRateSum = population.chromosomes.foldLeft(0f)(_ + _.getMutationRate())

        averageMutationRate = mutationRateSum / population.chromosomes.size
    }
  }

  //
}
