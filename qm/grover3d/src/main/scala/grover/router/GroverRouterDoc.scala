package grover.router

import com.wordnik.swagger.annotations._
import grover.model.GroverResult
import spray.routing._


@Api(value = "/compute", description = "Grover coin graph operations", consumes= "application/json",  produces = "application/json")
trait GroverRouterDoc {


    @ApiOperation(value = "Compute grover coin graph", httpMethod = "POST", response = classOf[GroverResult], consumes="application/json")
    @ApiImplicitParams(Array(
      new ApiImplicitParam(name = "body", value="request", required = true, dataType = "grover.model.GroverRequest", paramType = "body" )
    ))
    @ApiResponses(Array(
      new ApiResponse(code = 405, message = "Invalid request"),
      new ApiResponse(code = 200, message = "Request complete")
    ))
    def computeRoute: Route

}
