package grover.service

import java.util.regex.Pattern
import breeze.math.Complex

object ParseUtil {

  // https://stackoverflow.com/questions/50425322/c-regex-for-reading-complex-numbers
  val pattern = """
        |^
        | (?= [iI.\d+-] )               # Assertion that keeps it from matching empty string
        | (                             # (1 start), Real
        |      [+-]?
        |      (?:
        |           \d+
        |           (?: \. \d* )?
        |        |  \. \d+
        |      )
        |      (?: [eE] [+-]? \d+ )?
        |      (?! [iI.\d] )            # Assertion that separates real/imaginary
        | )?                            # (1 end)
        | (                             # (2 start), Imaginary
        |      \w*[+-]?\w*
        |      (?:
        |           (?:
        |                \d+
        |                (?: \. \d* )?
        |             |  \. \d+
        |           )
        |           (?: [eE] [+-]? \d+ )?
        |      )?
        |      [iI]
        | )?                            # (2 end)
        | $
      """.stripMargin

  val regex = Pattern.compile(pattern,Pattern.COMMENTS)
  
  
  def parseComplex(s:String):Option[Complex] = {
    
      val m = regex.matcher(s)
      if (!m.matches()) return None
      val re = getPart(m.group(1))
      val im = getPart(m.group(2))
      Some(Complex(re,im))
    
  }
  
  def getPart(s:String):Double = {
      if (s == null) return 0d
      var st = s.replace("i","").trim()
      if (st == "+" || st == "-") st = st + "0"
      if (st == "") 0d else st.toDouble
  }
  
}