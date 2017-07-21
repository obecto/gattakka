package playground
import akka.actor.{ActorRef, ActorSystem}
import com.obecto.gattakka.{BiologicalOperators, Evaluator, GeneticPopulation, Individual}
import com.obecto.gattakka.messages.individual.Initialize
import com.obecto.gattakka.genetics._

class CustomEvaluator(environmentalData: AnyVal, population: ActorRef) extends Evaluator(environmentalData,population){

   val fitness: Float = 0f

  def handleSignal(signalData: AnyVal): Unit = {
    //setFitness
  }
    /*  val x = chromosome.genes.head.asInstanceOf[BinaryGene].toDouble * 200 - 100
      val y = chromosome.genes(1).asInstanceOf[BinaryGene].toDouble * 200 - 100
      val temp1 = Math.sin(Math.sqrt(x * x + y * y))
      val temp2 = 1 + 0.001 * (x * x + y * y)
      val result = (0.5 + (temp1 * temp1 - 0.5) / (temp2 * temp2)).toFloat
     // println("Adding evaluated result... " + chromosome)
      chromosome.fitness = result
      evaluationResult.addEvaluatedChromosome(chromosome,isLast)*/
}

class CustomIndividualActor(chromosome: Chromosome) extends Individual(chromosome) {

  override def customReceive = {
    case Initialize(data) =>
      //TODO Extract data from chromosomes and connect with TA-LIB
  }

  def setSignal(when : Int): Unit ={
    //TODO Set signal Buy/Sell at ...
    //at the given moment ->
    // population ! Buy/Sell Event
  }
}

object RunGattakka extends App {

  val initialChromosomes = (1 to 100).map((i: Int) => {
    new Chromosome(Array[Gene[_]](
      BinaryGene(32), BinaryGene(32)
    ))
  })

  val system = ActorSystem("gattakka")

  val biologicalOperatorTypes = BiologicalOperators()

  val populationActor = system.actorOf(GeneticPopulation.props(
    classOf[CustomIndividualActor],
    biologicalOperatorTypes,
    classOf[CustomEvaluator],
    initialChromosomes,
    "Ko staa"
  ), "population")

  object abc extends AnyVal{

  }
}
