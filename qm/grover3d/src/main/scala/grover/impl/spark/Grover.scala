package grover.impl.spark

import grover.GroverI
import grover.utils.Config
import org.apache.log4j.{Level, Logger}
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable.ListBuffer

case class VType(value:Array[Double], connected:Array[Boolean])

object Grover  extends GroverI {


  def main(args: Array[String]) {

    Logger.getLogger("org").setLevel(Level.ERROR)
    Logger.getLogger("akka").setLevel(Level.ERROR)


    val result: Array[Array[Array[Double]]] = computeGraph(Array(0d, 0d, 1d, 0d, 0d, 0d), 10, 21)

    val formatted: String = getSquareGraphFormatted(result, true)

    println(formatted)

    Config.sparkContext.stop()

  }

  override def computeGraph(initialVector:Array[Double], iterations:Int, size:Int): Array[Array[Array[Double]]] = {

    val sc = Config.sparkContext

    // Set up grid
    val vs = new ListBuffer[(VertexId, VType)]
    val es = new ListBuffer[Edge[Int]]

    var vcount: Long = 0
    for (z: Int <- 0 until size) {
      for (y: Int <- 0 until size) {
        for (x: Int <- 0 until size) {
  
          val connected = Array(x > 0, x < size - 1, y < size - 1, y > 0, z < size - 1, z > 0)
  
          var value = Array(0d, 0d, 0d, 0d, 0d, 0d)
          if (z == Math.floor(size/2.0) && y == Math.floor(size/2.0) && x == Math.floor(size/2.0)) value = initialVector
          val p = (vcount, new VType(value, connected))
  
          vs += p
          vcount += 1
        }
      }
    }

    vs.foreach {
      case (id: VertexId, v: VType) => {
        val c = v.connected
        if (c(LEFT)) es += Edge(id, id - 1, LEFT)
        if (c(RIGHT)) es += Edge(id, id + 1, RIGHT)
        if (c(UP)) es += Edge(id, id + size, UP)
        if (c(DOWN)) es += Edge(id, id - size, DOWN)
        if (c(IN)) es += Edge(id, id + size*size, IN)
        if (c(OUT)) es += Edge(id, id - size*size, OUT)
      }
    }

    // GraphX RDDs
    val vertices: RDD[(VertexId, VType)] = sc.parallelize(vs)
    val edges: RDD[Edge[Int]] = sc.parallelize(es)
    val graph = Graph(vertices, edges)

    //    println(getSquareGraphFormatted(getSquareGraph(graph, size),true))
    //    printSum(graph)

    // Set up Pregel functions
    val initialMsg = Array[Double]()
    val zeroMsg = Array[Double](0d, 0d, 0d, 0d, 0d, 0d)

    def vprog(vertexId: VertexId, value: VType, message: Array[Double]): VType = {
      if (message.length == 0)
        value
      else {
        new VType(message, value.connected)
      }
    }

    def sendMsg(triplet: EdgeTriplet[VType, Int]): Iterator[(VertexId, Array[Double])] = {

      val v = triplet.srcAttr.value

      if (v(0) == 0 && v(1) == 0 && v(2) == 0 && v(3) == 0 && v(4) == 0 && v(5) == 0) {
        return Iterator()
      }

      val direction = triplet.toTuple._3

      val g: Array[Double] = grover(v)

      if (triplet.srcId == triplet.dstId) {
        // TODO Boundary reflection
      }

      val m: Array[Double] = mask(g, direction)
      if (norm(m) == 0) return Iterator()

      // zero message needed to tell pregel to run for that vertex
      Iterator((triplet.dstId, m), (triplet.srcId, zeroMsg))
    }

    def mergeMsg(m1: Array[Double], m2: Array[Double]): Array[Double] = {
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



  def getSquareGraph(g: Graph[VType, Int], size: Int): Array[Array[Array[Double]]] = {
    val result: Array[Array[Array[Double]]] = Array.ofDim[Double](size, size, size)
    
    g.vertices.collect.sortBy(_._1).foreach { p => {
      val anorm = norm(p._2.value)
      val i: Int = Math.floor(p._1 / (size*size)).toInt
      val j: Int = Math.floor(p._1 / size).toInt % size
      val k: Int = p._1.toInt % size
      result(i)(j)(k) = anorm
    }
    }

    result
  }


  def printSum(g: Graph[VType, Int]): Unit = {
    val sum = g.vertices.map(p => norm(p._2.value)).sum()
    println("sum = " + sum)
  }


}