package grover.router

import grover.model.{AuthInfo}
import grover.service.GroverService
import spray.routing.authentication.{ BasicAuth, UserPass }
import spray.routing.directives.AuthMagnet

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by gneotux on 20/03/15.
 */
trait Authenticator {

  def userService: GroverService

  def basicUserAuthenticator(implicit ec: ExecutionContext): AuthMagnet[AuthInfo] = {
    def validateUser(userPass: Option[UserPass]): Future[Option[AuthInfo]] = {
       Future.successful(Some(new AuthInfo()))
    }

    def authenticator(userPass: Option[UserPass]): Future[Option[AuthInfo]] = validateUser(userPass)

    BasicAuth(authenticator _, realm = "Private API")
  }
}