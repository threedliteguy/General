package grover.model

import com.wordnik.swagger.annotations.{ApiModelProperty, ApiModel}
import spray.json.DefaultJsonProtocol

import scala.annotation.meta.field

@ApiModel(description = "Grover request")
case class GroverRequest(
                          @(ApiModelProperty @field)(value = "Comma-separated initial 4 coordinate vector")
                          initialVector:String,
                          @(ApiModelProperty @field)(value = "Iterations")
                          iterations:Int,
                          @(ApiModelProperty @field)(value = "Graph square dimension")
                          size:Int
                        ) {
}
object GroverRequest extends DefaultJsonProtocol{
  implicit val userFormat = jsonFormat3(GroverRequest.apply)
}
