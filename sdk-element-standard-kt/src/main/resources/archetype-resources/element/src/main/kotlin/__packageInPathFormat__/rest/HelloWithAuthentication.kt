package ${package}.rest

import ${package}.HelloWorldApplication
import ${package}.service.GreetingService
import dev.getelements.elements.sdk.Element
import dev.getelements.elements.sdk.ElementSupplier
import dev.getelements.elements.sdk.jakarta.rs.AuthSchemes.SESSION_SECRET
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType

@Tag(name = HelloWorldApplication.OPENAPI_TAG)
@Path("/hellowithauthentication")
class HelloWithAuthentication {

    private val element: Element = ElementSupplier
        .getElementLocal(HelloWithAuthentication::class.java)
        .get()

    private val greetingService: GreetingService = element
        .serviceLocator
        .getInstance(GreetingService::class.java)

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.TEXT_PLAIN)
    @Operation(
        summary = "Greeting with login check",
        description = "Checks if the session token in the header corresponds to at least a USER level user.",
        security = [SecurityRequirement(name = SESSION_SECRET)]
    )
    fun sayHelloWithAuth(): String = greetingService.getGreeting()

}
