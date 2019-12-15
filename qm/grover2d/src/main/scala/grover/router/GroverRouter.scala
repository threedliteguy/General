package grover.router

import grover.model.GroverRequest
import grover.service.{GroverServiceI, GroverService}

import spray.http.MediaTypes._
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._
import spray.routing.{ HttpService, Route }
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global


trait GroverRouter extends HttpService with GroverRouterDoc {
  self: Authenticator =>

  val groverService: GroverService = GroverServiceI

  val userOperations: Route = computeRoute


  override def computeRoute: Route = path("compute") {
      post {
        authenticate(basicUserAuthenticator) { authInfo =>
          entity(as[GroverRequest]) { groverRequest =>
            respondWithMediaType(`application/json`) {
              onComplete(groverService.compute(groverRequest)) {
                case Success(Some(a)) => complete(OK, a)
                case Success(None) => complete(NotAcceptable, "Invalid request")
                case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
              }
            }
          }
        }
      }
    }

}
