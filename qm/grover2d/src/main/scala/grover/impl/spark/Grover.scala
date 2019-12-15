package grover.impl.spark

import breeze.math.Complex

import grover.GroverI
import grover.utils.Config
import org.apache.log4j.{Level, Logger}
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable.ListBuffer

case class VType(value:Array[Complex], connected:Array[Boolean])

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
        if (y == Math.floor(size/2.0) && x == Math.floor(size/2.0)) value = initialVector
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
    val edges: RDD[Edge[Int]] = spark.sparkContext.parallelize(es)
    val graph = Graph(vertices, edges)

    //    println(getSquareGraphFormatted(getSquareGraph(graph, size)))
    //    printSum(graph)

    // Set up Pregel functions
    val initialMsg = Array[Complex]()
    val zeroMsg = Array[Complex](Complex.zero, Complex.zero, Complex.zero, Complex.zero)

    def vprog(vertexId: VertexId, value: VType, message: Array[Complex]): VType = {
      if (message.length == 0)
        value
      else {
        //println(vertexId, message.toList)
        new VType(message, value.connected)
      }
    }

    def sendMsg(triplet: EdgeTriplet[VType, Int]): Iterator[(VertexId, Array[Complex])] = {

      val v = triplet.srcAttr.value

      if (v(0) == 0 && v(1) == 0 && v(2) == 0 && v(3) == 0) {
        return Iterator()
      }

      val direction = triplet.toTuple._3

      val g: Array[Complex] = grover(v)

      if (triplet.srcId == triplet.dstId) {
        // TODO Boundary reflection
      }

      val m: Array[Complex] = mask(g, direction)
      if (norm(m) == 0) return Iterator()

      // zero message needed to tell pregel to run for that vertex
      Iterator((triplet.dstId, m), (triplet.srcId, zeroMsg))
    }

    def mergeMsg(m1: Array[Complex], m2: Array[Complex]): Array[Complex] = {
      m1.indices.map(i => m1(i) + m2(i)).toArray
    }



    val result = graph.pregel(initialMsg,
      iterations,
      EdgeDirection.Out)(
      vprog,
      sendMsg,
      mergeMsg)


    getSquareGraph(result, size)


  }



  def getSquareGraph(g: Graph[VType, Int], size: Int): Array[Array[Complex]] = {
    
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