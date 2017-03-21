import akka.actor.{Actor, ActorSystem, Props}
import akka.pattern.ask
import akka.routing.RoundRobinPool
import akka.util.Timeout
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.io.Source


class ChildActor extends Actor {


  override def receive: Receive = {
    case msg: String =>
      val sizeOfLine = msg.split("[ ,!.]+").size
      sender() ! sizeOfLine
  }
}

class ParentActor extends Actor {

  implicit val timeout = Timeout(1000 seconds)

  val system = ActorSystem("ActorRouterSystem")
  val childRouter = context.actorOf(RoundRobinPool(5).props(Props[ChildActor]), name = "childPoolRouter")

  val system2 = ActorSystem("ActorRouterSystem")
  val router = system2.actorOf(Props[ParentActor])
  var count = 0

  override def receive: Receive = {
    case msg: String =>
        val fileData = Source.fromFile(msg).getLines().toList
        val lines =(for(i <- fileData.indices) yield childRouter ? fileData(i).toString).toList
        val listOfWordCount = Await.result(Future.sequence(lines), 10 seconds)
        listOfWordCount.foreach {
          case x: Int => count+= x
        }
        sender() ! count
    case _ => sender() ! "Not a File NAme"
  }
}

object FileWordCounter extends App {
  implicit val timeout = Timeout(1000 seconds)
  val system = ActorSystem("ActorRouterSystem")
  val router = system.actorOf(Props[ParentActor])
  val fileName = "./src/main/resources/lines.txt"
    val res = router ? fileName
  res map (x=>print("Total number of words in the file are : " + x))
}
