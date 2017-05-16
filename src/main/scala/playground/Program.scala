package playground
import akka.actor.{ ActorSystem, Props, Inbox, ActorRef }
import scala.concurrent.duration._
import com.obecto.actors._

object RunGattakka extends App {
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

