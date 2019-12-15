package grover.service

import breeze.math.Complex

import java.util.regex.Pattern
import grover.GroverI
import grover.model.{GroverRequest, GroverResult}
import grover.utils.Config

import breeze.math.Complex

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import scala.util.Try

trait GroverService {

  def compute(groverRequest:GroverRequest): Future[Option[GroverResult]]

}

object GroverServiceI extends GroverService {
  
  def parseComplex(s:String):Complex = {
    val t = Try(ParseUtil.parseComplex(s))
    if (t.isFailure || t.get.isEmpty) { println("Invalid complex number format: "+ s +": "+ t.failed.get.getMessage) }
    t.get.get
  }


  override def compute(groverRequest:GroverRequest): Future[Option[GroverResult]] = {
    Future {
      val array = groverRequest.initialVector.split(",").map(parseComplex(_))
      val impl: GroverI = Config.getImpl()     
      Some(new GroverResult(impl.getSquareGraphFormatted(impl.computeGraph(array, groverRequest.iterations, groverRequest.size), false)))
    }
  }

}