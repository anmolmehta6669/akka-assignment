import akka.actor.{Actor, ActorSystem, Props}
import akka.util.Timeout
import scala.concurrent.duration._


class Person extends Actor {
  override def receive: Receive = {
    case num: Any => {
    }
  }
}

class BookingReception extends Actor {
  var seats = 4;

  override def receive: Receive = {
    case num: Int =>
      println(s"from: ${sender().path.name.toUpperCase}")
      implicit val timeout = Timeout(10 seconds)
      if (num <= seats) {
        println(s"available:  $seats")
        seats -= num
        println(s"Booked: $num ")
        println(s"After booking:  $seats \n")
      }
      else {
        println(s" $num Seats Not Available ")
      }
  }
}


class BookingQueue extends Actor {

  override def receive: Receive = {
    case request: Int =>
      val bookingReception = context.actorSelection("../Reception")
      bookingReception forward (request)
  }
}


object BookMyShow extends App {
  implicit val timeout = Timeout(10 seconds)

  val system = ActorSystem("TicketBooking")

  val propsPerson = Props[Person]

  val reception = system.actorOf(Props[BookingReception], "Reception")
  val bookingQueue1 = system.actorOf(Props[BookingQueue])
  val bookingQueue2 = system.actorOf(Props[BookingQueue])

  val person1 = system.actorOf(propsPerson, "Person1")
  val person2 = system.actorOf(propsPerson, "Person2")
  val person3 = system.actorOf(propsPerson, "Person3")
  val person4 = system.actorOf(propsPerson, "Person4")
  val person5 = system.actorOf(propsPerson, "Person5")

  bookingQueue1.tell(1, person1)
  bookingQueue2.tell(2, person2)
  bookingQueue1.tell(3, person3)
  bookingQueue1.tell(2, person5)
  bookingQueue1.tell(1, person4)

}


