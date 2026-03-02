#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import dev.get${artifactId}s.${artifactId}s.sdk.model.Headers;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;

import static dev.get${artifactId}s.${artifactId}s.sdk.jakarta.rs.AuthSchemes.SESSION_SECRET;
import static io.swagger.v3.oas.annotations.enums.SecuritySchemeIn.HEADER;
import static io.swagger.v3.oas.annotations.enums.SecuritySchemeType.APIKEY;

@OpenAPIDefinition(
        info = @Info(
                title = "Example Element",
                description = "An example ${artifactId}.",
                contact = @Contact(
                        url = "https://namazustudios.com",
                        email = "info@namazustudios.com",
                        name = "Namazu Studios"
                )
        ),
        externalDocs = @ExternalDocumentation(
                url = "https://namazustudios.com/docs",
                description = "Please see the Namazu Elements Manual for more information."
        ),
        security = {
                @SecurityRequirement(name = SESSION_SECRET)
        }
)
@SecuritySchemes({
        @SecurityScheme(
                type = APIKEY,
                in = HEADER,
                name = SESSION_SECRET,
                paramName = SESSION_SECRET,
                description = "Session secret required for authenticated endpoints")
})
public class OpenAPISecurityConfig {}
