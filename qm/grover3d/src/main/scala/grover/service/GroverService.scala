package grover.service


import grover.GroverI
import grover.model.{GroverRequest, GroverResult}
import grover.utils.Config


import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future



trait GroverService {

  def compute(groverRequest:GroverRequest): Future[Option[GroverResult]]

}

object GroverServiceI extends GroverService {


  override def compute(groverRequest:GroverRequest): Future[Option[GroverResult]] = {
    Future {
      val array = groverRequest.initialVector.split(",").map(_.toDouble)
      val impl: GroverI = Config.getImpl()
      Some(new GroverResult(impl.getSquareGraphFormatted(impl.computeGraph(array, groverRequest.iterations, groverRequest.size), false)))
    }
  }

}