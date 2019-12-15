package grover

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import router.ApiRouterActor
import grover.service.GroverServiceI
import spray.can.Http
import utils.Config._

import scala.concurrent.duration._

// Spray and swagger setup courtesy of https://github.com/Gneotux/spray-swagger-slick-seed (Apache license)

object Boot //extends App 
{

  def main(args:Array[String]) {
    
      
    implicit val system = ActorSystem(app.systemName)
  
    val userActor: ActorRef = system.actorOf(Props(classOf[ApiRouterActor],GroverServiceI), app.groverServiceName)
  
    implicit val timeout = Timeout(60.seconds)
    IO(Http) ? Http.Bind(userActor, interface = app.interface, port = app.port)
    
  }

}
