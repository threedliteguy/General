package grover
import breeze.math.Complex

trait GroverI {

  def computeGraph(initialVector:Array[Complex], iterations:Int, size:Int): Array[Array[Array[Complex]]]

  def getSquareGraphFormatted(a: Array[Array[Array[Complex]]], newlines:Boolean): String = {
    val size = a.length
    val sb = new StringBuilder()
    sb.append("[")
    if (newlines) sb.append("\n")
    for (z: Int <- 0 until size) {
      if (z > 0) sb.append(",")
      sb.append("[")
      for (y: Int <- 0 until size) {
        if (y > 0) sb.append(",")
        sb.append("[")
        for (x: Int <- 0 until size) {
          if (x > 0) sb.append(", ")
          sb.append(a(z)(y)(x).abs)
        }
        sb.append("]")
        if (newlines) sb.append("\n")
      }
      sb.append("]")
      if (newlines) sb.append("\n")
    }
    sb.append("]")
    if (newlines) sb.append("\n")
    sb.toString()
  }

  
  def grover(a: Array[Complex]): Array[Complex] = {
    Array(
      -2*a(0) + a(1) + a(2) + a(3) + a(4) + a(5),
      a(0) - 2*a(1) + a(2) + a(3) + a(4) + a(5),
      a(0) + a(1) - 2*a(2) + a(3) + a(4) + a(5),
      a(0) + a(1) + a(2) - 2*a(3) + a(4) + a(5),
      a(0) + a(1) + a(2) + a(3) - 2*a(4) + a(5),
      a(0) + a(1) + a(2) + a(3) + a(4) - 2*a(5)
    ).map(_ / 3d)
  }

  /*
   
   See: https://iopscience.iop.org/article/10.1088/1367-2630/15/7/073041
   3D grover coin (6x6) sagemath code:

import numpy
ones=matrix( numpy.array(map(lambda x: int(x), numpy.ones((6,6)).ravel() )).reshape(6,6) )
g3=(ones - 3*matrix.identity(6))*(1/3)

[-2/3  1/3  1/3  1/3  1/3  1/3]
[ 1/3 -2/3  1/3  1/3  1/3  1/3]
[ 1/3  1/3 -2/3  1/3  1/3  1/3]
[ 1/3  1/3  1/3 -2/3  1/3  1/3]
[ 1/3  1/3  1/3  1/3 -2/3  1/3]
[ 1/3  1/3  1/3  1/3  1/3 -2/3]

  */

  
  val LEFT = 0
  val RIGHT = 1
  val UP = 2
  val DOWN = 3
  val IN = 4
  val OUT = 5

  def mask(a: Array[Complex], d: Int): Array[Complex] = {
    d match {
      case LEFT  => Array(a(0), Complex.zero, Complex.zero, Complex.zero, Complex.zero, Complex.zero)
      case RIGHT => Array(Complex.zero, a(1), Complex.zero, Complex.zero, Complex.zero, Complex.zero)
      case UP    => Array(Complex.zero, Complex.zero, a(2), Complex.zero, Complex.zero, Complex.zero)
      case DOWN  => Array(Complex.zero, Complex.zero, Complex.zero, a(3), Complex.zero, Complex.zero)
      case IN    => Array(Complex.zero, Complex.zero, Complex.zero, Complex.zero, a(4), Complex.zero)
      case OUT   => Array(Complex.zero, Complex.zero, Complex.zero, Complex.zero, Complex.zero, a(5))
    }
  }

  def norm(a: Array[Complex]): Complex = {
    a.indices.map(i => a(i) * a(i)).reduce(_+_)
  }


}
