package grover.impl.aggregateMessages

import breeze.math.Complex

import grover.GroverI
import grover.utils.Config
import org.apache.log4j.{Level, Logger}
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable.ListBuffer

import grover.GroverI
import grover.utils.Config
import org.apache.log4j.{Level, Logger}
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SQLContext

import scala.collection.mutable.ListBuffer

case class VType(value:Array[Complex], connected:Array[Boolean])
case class EType(value:Int)
case class MType(value:Array[Complex])

// Using GraphX aggregateMessages api similar to dataframes BeliefPropagation example
object Grover  extends GroverI {


  def main(args: Array[String]) {

    Logger.getLogger("org").setLevel(Level.ERROR)
    Logger.getLogger("akka").setLevel(Level.ERROR)


    val result: Array[Array[Complex]] = computeGraph(Array(Complex.zero, Complex.zero, Complex.one, Complex.zero), 10, 21)

    val formatted: String = getSquareGraphFormatted(result, true)

    println(formatted)

    Config.spark.stop()

  }

  override def computeGraph(initialVector:Array[Complex], iterations:Int, size:Int): Array[Array[Complex]] = {

    val spark = Config.spark
 

    // Set up grid
    val vs = new ListBuffer[(VertexId, VType)]
    val es = new ListBuffer[Edge[Int]]

    var vcount: Long = 0
    for (y: Int <- 0 until size) {
      for (x: Int <- 0 until size) {

        val connected = Array(x > 0, x < size - 1, y < size - 1, y > 0)

        var value = Array(Complex.zero, Complex.zero, Complex.zero, Complex.zero)
        if (y == Math.floor(size / 2.0) && x == Math.floor(size / 2.0)) value = initialVector
        val p = (vcount, new VType(value, connected))

        vs += p
        vcount += 1
      }
    }

    vs.foreach {
      case (id: VertexId, v: VType) => {
        val c = v.connected
        if (c(LEFT)) es += Edge(id, id - 1, LEFT)
        if (c(RIGHT)) es += Edge(id, id + 1, RIGHT)
        if (c(UP)) es += Edge(id, id + size, UP)
        if (c(DOWN)) es += Edge(id, id - size, DOWN)
      }
    }

    // GraphX RDDs
    val vertices: RDD[(VertexId, VType)] = spark.sparkContext.parallelize(vs)
    val edges: RDD[Edge[EType]] = spark.sparkContext.parallelize(es.map(p=>Edge(p.srcId,p.dstId,new EType(p.attr))))
    var graph: Graph[VType, EType] = Graph(vertices, edges)


    def getOutMessage(v:Array[Complex], direction:Int): Array[Complex] = {
       val g: Array[Complex] = grover(v)
       val m: Array[Complex] = mask(g, direction)
      m
    }

    def mergeMsg(message1:MType, message2:MType): MType = {
      val m1 = message1.value
      val m2 = message2.value
      new MType(m1.indices.map(i => m1(i) + m2(i)).toArray)
    }

    for (iter <- Range(0,iterations)) {

      val msgs = graph.aggregateMessages(
        (ctx: EdgeContext[VType, EType, MType]) => {
          val msg = getOutMessage(ctx.srcAttr.value, ctx.attr.value)
          ctx.sendToDst(new MType(msg))
      },
        (m1:MType,m2:MType) => mergeMsg(m1, m2)
      )

      graph = graph.outerJoinVertices(msgs) {
        case (vID, vAttr, optMsg) =>
          if (optMsg.isEmpty) {
            vAttr
          } else {
            new VType(optMsg.get.value, vAttr.connected)
          }
      }

    }

    getSquareGraph(graph, size)

  }

  def getSquareGraph(g: Graph[VType, EType], size: Int): Array[Array[Complex]] = {
    val result: Array[Array[Complex]] = Array.ofDim[Complex](size, size)

    g.vertices.collect.sortBy(_._1).foreach { p => {
      val anorm = norm(p._2.value)
      val i: Int = Math.floor(p._1 / size).toInt
      val j: Int = p._1.toInt % size
      result(i)(j) = anorm
    }
    }

    result
  }


  def printSum(g: Graph[VType, Int]): Unit = {
    val sum = g.vertices.map(p => norm(p._2.value)).sum()
    println("sum = " + sum)
  }


}