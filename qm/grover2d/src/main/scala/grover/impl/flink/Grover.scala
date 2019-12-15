package grover.impl.flink

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
import org.apache.flink.graph.gsa._
import org.apache.flink.graph.scala.Graph


import scala.collection.mutable.ListBuffer

import org.apache.flink.api.scala._

import org.apache.flink.graph.{EdgeDirection, Edge, Vertex}



case class VType(var value:Array[Complex], connected:Array[Boolean])
case class MType(var value:Array[Complex])

object Grover extends GroverI {


  def main(args: Array[String]) {

    val result: Array[Array[Complex]] = computeGraph(Array(Complex.zero, Complex.zero, Complex.one, Complex.zero), 10, 21)

    val formatted: String = getSquareGraphFormatted(result, true)

    println(formatted)

  }

  override def computeGraph(initialVector:Array[Complex], iterations:Int, size:Int): Array[Array[Complex]] = {

    val env:ExecutionEnvironment = Config.flinkEnvironment

    // Set up grid
    val vs = new ListBuffer[Vertex[Long, VType]]
    val es = new ListBuffer[Edge[Long, Int]]


    // Hack: Unfortunately I dont know a way to send a message back to the origin during the first superstep when all messages are outgoing (to set the origin vertex to 0) in Gelly like you can in GraphX.
    // So setting surrounding values to a small number instead of 0 forces vertex processing.
    // Only really needs to be done on the vertices adjacent to origin but to avoid potential avoid artifacts I set everywhere,
    val smallvalue = Complex(0.00000000000001d,0.00000000000001d)

    var vcount: Long = 0
    for (y: Int <- 0 until size) {
      for (x: Int <- 0 until size) {

        val connected = Array(x > 0, x < size - 1, y < size - 1, y > 0)


        var value = Array(smallvalue, smallvalue, smallvalue, smallvalue)
        if (y == Math.floor(size/2.0) && x == Math.floor(size/2.0)) value = initialVector
        val p = new Vertex(vcount, new VType(value, connected))

        vs += p
        vcount += 1
      }
    }

    vs.foreach {
      case v:Vertex[Long, VType] => {
        val c = v.getValue.connected
        val id = v.getId
        if (c(LEFT)) es += new Edge(id, id - 1, LEFT)
        if (c(RIGHT)) es += new Edge(id, id + 1, RIGHT)
        if (c(UP)) es += new Edge(id, id + size, UP)
        if (c(DOWN)) es += new Edge(id, id - size, DOWN)
      }
    }

    // Create Graph DataSet

    val vertices: DataSet[Vertex[Long, VType]] = env.fromCollection(vs)
    val edges: DataSet[Edge[Long,Int]] = env.fromCollection(es)
    val graph: Graph[Long, VType, Int] = Graph.fromDataSet(vertices, edges, env)

    //        println(getSquareGraphFormatted(getSquareGraph(graph, size), true))
    //        printSum(graph)

    val zeroMsg = Array[Complex](Complex.zero, Complex.zero, Complex.zero, Complex.zero)

    final class GatherGrover extends GatherFunction[VType, Int, MType] {

      override def gather(neighbor: Neighbor[VType, Int]): MType = {

        val v = neighbor.getNeighborValue.value

        if (v(0) == Complex.zero && v(1) == Complex.zero && v(2) == Complex.zero && v(3) == Complex.zero) {
          return new MType(zeroMsg)
        }

        val direction = neighbor.getEdgeValue

        val g: Array[Complex] = grover(v)

        val m: MType = new MType(mask(g, direction))
        if (norm(m.value) == 0) return new MType(zeroMsg)

        m

      }
    }

    final class SumGrover extends SumFunction[VType, Int, MType] {
      override def sum(m1: MType, m2: MType): MType = {
        val s = m1.value.indices.map(i => m1.value(i) + m2.value(i)).toArray
        if (norm(s) == 0) return new MType(zeroMsg)
        new MType(s)
      }
    }

    final class ApplyGrover extends ApplyFunction[Long, VType, MType] {

      override def apply(m: MType, v: VType) = {
        if (norm(m.value).abs > 0) {
          v.value = m.value
          setResult(v)
        }

      }
    }

    val parameters = new GSAConfiguration
    parameters.setDirection(EdgeDirection.OUT)
    val result = graph.runGatherSumApplyIteration(new GatherGrover, new SumGrover, new ApplyGrover, iterations, parameters)

    val r: Array[Array[Complex]] = getSquareGraph(result, size)
    printSum(result)

    r
  }


  def getSquareGraph(g: Graph[Long, VType, Int], size: Int): Array[Array[Complex]] = {
    val result: Array[Array[Complex]] = Array.ofDim[Complex](size, size)

    g.getVertices.collect.sortBy(_.getId).foreach { p => {
      val anorm = norm(p.getValue.value)
      val i: Int = Math.floor(p.getId / size).toInt
      val j: Int = p.getId.toInt % size
      result(i)(j) = anorm
    }
    }

    result
  }


  def printSum(g: Graph[Long, VType, Int]): Unit = {
    val sum = g.getVertices.map(p => norm(p.getValue.value)).reduce(_+_).collect().sum
    println("sum = " + sum)
  }


}