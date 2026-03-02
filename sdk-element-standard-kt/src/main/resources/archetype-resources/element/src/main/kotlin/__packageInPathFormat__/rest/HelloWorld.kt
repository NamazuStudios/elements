package ${package}.rest

import ${package}.HelloWorldApplication
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType

@Tag(name = HelloWorldApplication.OPENAPI_TAG)
@Path("/helloworld")
class HelloWorld {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(summary = "Hello world probe", description = "Returns a simple greeting")
    fun sayHello(): String = "Hello world!"

}
