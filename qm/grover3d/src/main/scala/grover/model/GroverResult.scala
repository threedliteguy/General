package grover.model

import com.wordnik.swagger.annotations.{ApiModelProperty, ApiModel}
import spray.json.DefaultJsonProtocol

import scala.annotation.meta.field

@ApiModel(description = "Grover result")
case class GroverResult(
                         @(ApiModelProperty @field)(value = "result surface plot")
                         result:String
                       ) {

}
object GroverResult extends DefaultJsonProtocol{
  implicit val userFormat = jsonFormat1(GroverResult.apply)
}

