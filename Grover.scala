package test

import org.apache.log4j.{Level, Logger}
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable.ListBuffer

case class VType(value:Array[Double], connected:Array[Boolean])

// The Grover coin is used in quantum algorithms
// This is an example using Spark GraphX Pregel 

object Grover {

  def main(args: Array[String]) {

    Logger.getLogger("org").setLevel(Level.ERROR)
    Logger.getLogger("akka").setLevel(Level.ERROR)

    val conf = new SparkConf().setAppName("Grover");
    val sc = new SparkContext(conf)

    // Set up grid
    val size = 21

    val vs = new ListBuffer[(VertexId, VType)]

    val es = new ListBuffer[Edge[Int]]
    val LEFT = 0
    val RIGHT = 1
    val UP = 2
    val DOWN = 3


    var vcount:Long = 0
    for (y:Int <- 0 until size) {
      for (x:Int <- 0 until size) {

        val connected = Array(x > 0 , x < size-1, y < size-1, y > 0 )

        var value = Array(0d,0d,0d,0d)
        if (y == 10 && x == 10) value = Array(0d,0d,1d,0d)
        val p = (vcount, new VType(value, connected))

        vs += p
        vcount += 1
      }
    }

    vs.foreach {
      case (id:VertexId,v:VType) => {
        val c = v.connected
        if (c(LEFT)) es += Edge(id, id-1, LEFT)
        if (c(RIGHT)) es += Edge(id, id+1, RIGHT)
        if (c(UP)) es += Edge(id, id+size, UP)
        if (c(DOWN)) es += Edge(id, id-size, DOWN)
      }
    }

    // GraphX RDDs
    val vertices: RDD[(VertexId, VType)] = sc.parallelize(vs)
    val edges: RDD[Edge[Int]] = sc.parallelize(es)
    val graph = Graph(vertices, edges)


    def norm(a:Array[Double]):Double = {
      a.indices.map(i => a(i) * a(i)).sum
    }

    def printSquareGraph(g:Graph[VType,Int], size:Int): Unit = {
      // Print the graph upsidedown
      g.vertices.collect.sortBy(_._1).foreach{ p=> { if (p._1 % size == 0) println ; print(" "+norm(p._2.value)) } }
      println
      // Print total prob
      val sum = g.vertices.map(p=>norm(p._2.value)).sum()
      println("sum = "+sum)
    }

    printSquareGraph(graph, size)



    // Set up Pregel functions
    val initialMsg = Array[Double]()
    val zeroMsg = Array[Double](0d,0d,0d,0d)

    def vprog(vertexId: VertexId, value: VType, message: Array[Double]): VType = {
      if (message.length == 0)
        value
      else {
        println(vertexId,message.toList)
        new VType(message, value.connected)
      }
    }

    def sendMsg(triplet: EdgeTriplet[VType, Int]): Iterator[(VertexId, Array[Double])] = {

      val v = triplet.srcAttr.value

      if (v(0) == 0 && v(1) == 0 && v(2) == 0 && v(3) == 0) {
        return Iterator()
      }

      val direction = triplet.toTuple._3

      val g:Array[Double] = grover(v)

      if (triplet.srcId == triplet.dstId) {
        // TODO Boundary reflection
      }

      val m:Array[Double] = mask(g,direction)
      if (norm(m)==0) return Iterator()

      // zero message needed to tell pregel to run for that vertex
      Iterator( (triplet.dstId, m) , (triplet.srcId, zeroMsg))
    }

    def mergeMsg(m1: Array[Double], m2: Array[Double]): Array[Double] = {
      m1.indices.map(i => m1(i) + m2(i)).toArray
    }

    def grover(a:Array[Double]): Array[Double] = {
      Array(
        -a(0)+a(1)+a(2)+a(3),
        a(0)-a(1)+a(2)+a(3),
        a(0)+a(1)-a(2)+a(3),
        a(0)+a(1)+a(2)-a(3)
      ).map(_ * 0.5d)
    }

    def mask(a:Array[Double], d:Int) : Array[Double] = {
      d match {
        case LEFT  => Array(a(0),0,0,0)
        case RIGHT => Array(0,a(1),0,0)
        case UP    => Array(0,0,a(2),0)
        case DOWN  => Array(0,0,0,a(3))
      }
    }


    val result = graph.pregel(initialMsg,
      10,
      EdgeDirection.Out)(
      vprog,
      sendMsg,
      mergeMsg)


    printSquareGraph(result,size)


    sc.stop()

  }




}
