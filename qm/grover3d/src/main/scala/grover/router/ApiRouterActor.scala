package grover.router

import akka.actor.{ Actor, ActorLogging }
import com.gettyimages.spray.swagger.SwaggerHttpService
import com.wordnik.swagger.model.ApiInfo
import grover.service.GroverService


import scala.reflect.runtime.universe._


class ApiRouterActor(service: GroverService) extends Actor with GroverRouter with ActorLogging with Authenticator {

  override val userService = service

  val swaggerService = new SwaggerHttpService {
    override def apiTypes = Seq(typeOf[GroverRouterDoc])
    override def apiVersion = "0.1"
    override def baseUrl = "/"
    override def docsPath = "api-docs"
    override def actorRefFactory = context
    override def apiInfo = Some(new ApiInfo("Grover Coin graph computation API", "", "", "", "", ""))
  }

  def actorRefFactory = context

  def receive = runRoute(
    userOperations ~
    swaggerService.routes ~
    get {
      pathPrefix("") { pathEndOrSingleSlash {
          getFromResource("swagger-ui/index.html")
        }
      } ~
      pathPrefix("webjars") {
        getFromResourceDirectory("META-INF/resources/webjars")
      } ~
        pathPrefix("html") {
          getFromResourceDirectory("html")
        }
    }
  )

}