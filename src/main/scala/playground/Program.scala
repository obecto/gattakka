package playground
import akka.actor.{ ActorSystem, Props, Inbox }
// import scala.concurrent.duration._
import com.obecto.actors._
import com.obecto.operators._
import com.obecto.genetics._

object RunGattakka extends App {
  val pipeline = new Pipeline(List(), 1000)

  // pipeline.addOperator(new OffspringOperator(new BinaryMutationStrategy(), new TruncationSelectionStrategy()))
  pipeline.addOperator(new PrintOperator())
  pipeline.addOperator(new ElitismOperator(new TruncationSelectionStrategy()))
  pipeline.addOperator(new OffspringOperator(new BinaryMutationStrategy(0.01f), new RouletteWheelSelectionStrategy()))

  var generation = new Generation(0)
  for (i <- 0 until pipeline.targetPopulationSize) {
    generation.chromosomes += new Chromosome(Array[Gene[_]](
      BinaryGene(32), BinaryGene(32)
    ))
  }

  for (i <- 0 to 40) {
    for (chromosome <- generation.chromosomes) {
      val x = chromosome.genes(0).asInstanceOf[BinaryGene].toDouble * 200 - 100
      val y = chromosome.genes(1).asInstanceOf[BinaryGene].toDouble * 200 - 100
      val temp1 = Math.sin(Math.sqrt(x * x + y * y));
      val temp2 = 1 + 0.001 * (x * x + y * y);
      val result = 0.5 + (temp1 * temp1 - 0.5) / (temp2 * temp2);

      chromosome.calculatedFitness = (1 - result).toFloat;
    }

    generation.resortChromosomes()
    generation.recomputeValues()

    println(s"Generation: ${generation.sequentialId}\t| Max F: ${generation.maxFitness}\t| Min F: ${generation.minFitness}\t| Population: ${generation.chromosomes.length}")
    // println(s"\tBest: ${generation.chromosomes(0).calculatedFitness}")
    // println(s"\tBest: ${generation.chromosomes(0).genes(0).asInstanceOf[BinaryGene].asDouble * 200 - 100}, ${generation.chromosomes(0).genes(1).asInstanceOf[BinaryGene].asDouble * 200 - 100}")
    // println(s"\tBest: ${generation.chromosomes(0).genes(0).value}, ${generation.chromosomes(0).genes(1).value}")
    generation = pipeline.apply(generation)
  }

  // Create the 'helloakka' actor system
  val system = ActorSystem("gattakka")

  // Create the 'greeter' actor
  val population = system.actorOf(Props[GeneticPopulationActor], "population")

  // Create an "actor-in-a-box"
  val inbox = Inbox.create(system)

  // Tell the 'greeter' to change its 'greeting' message
  // population.tell(messages.Greeting("yay"), ActorRef.noSender)

  system.terminate()
}

