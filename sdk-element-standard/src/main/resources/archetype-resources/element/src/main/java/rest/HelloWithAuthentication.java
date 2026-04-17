#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.rest;

import ${package}.service.GreetingService;
import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.ElementSupplier;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import static ${package}.HelloWorldApplication.OPENAPI_TAG;
import static dev.getelements.elements.sdk.jakarta.rs.AuthSchemes.SESSION_SECRET;


@Tag(name = OPENAPI_TAG)
@Path("/hellowithauthentication")
public class HelloWithAuthentication {

    private final Element element = ElementSupplier
            .getElementLocal(HelloWithAuthentication.class)
            .get();

    private final GreetingService greetingService = element
            .getServiceLocator()
            .getInstance(GreetingService.class);


    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.TEXT_PLAIN)
    @Operation(
            summary = "Greeting with login check",
            description = "Checks if the session token in the header corresponds to at least a USER level user.",
            security = { @SecurityRequirement(name = SESSION_SECRET) }
    )
    public String sayHelloWithAuth() {
        return greetingService.getGreeting();
    }

}
