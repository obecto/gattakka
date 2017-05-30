package playground
import akka.actor.{ ActorSystem, Props, Inbox }
// import scala.concurrent.duration._
import com.obecto.actors._
import com.obecto.operators._
import com.obecto.genetics._

object RunGattakka extends App {
  val pipeline = new Pipeline(List(
    new InitializationGenerator(() => {
      new Chromosome(Array[Gene[_]](
        BinaryGene(32), BinaryGene(32)
      ))
    }),
    new BinaryMutationGenerator(1, new RouletteWheelSelectionStrategy(), 0.01f)
  ))

  // pipeline.addOperator(new OffspringOperator(new BinaryMutationStrategy(), new TruncationSelectionStrategy()))

  // Create the 'helloakka' actor system
  val system = ActorSystem("gattakka")

  // Create the 'greeter' actor
  val populationActor = system.actorOf(Props(classOf[GeneticPopulationActor], Props[EvaluatorActor], pipeline), "population")

  // Create an "actor-in-a-box"
  val inbox = Inbox.create(system)

  // Tell the 'greeter' to change its 'greeting' message
  // population.tell(messages.Greeting("yay"), ActorRef.noSender)

  system.terminate()
}

