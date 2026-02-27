#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import ${package}.rest.ExampleContent;
import ${package}.rest.HelloWithAuthentication;
import ${package}.rest.HelloWorld;
import dev.get${artifactId}s.${artifactId}s.sdk.annotation.ElementDefaultAttribute;
import dev.get${artifactId}s.${artifactId}s.sdk.annotation.ElementServiceExport;
import dev.get${artifactId}s.${artifactId}s.sdk.annotation.ElementServiceImplementation;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import jakarta.ws.rs.core.Application;

import java.util.Set;

@ElementServiceImplementation
@ElementServiceExport(Application.class)
public class HelloWorldApplication extends Application {

    @ElementDefaultAttribute("true")
    public static final String AUTH_ENABLED = "dev.get${artifactId}s.${artifactId}s.auth.enabled";

    @ElementDefaultAttribute("example-${artifactId}")
    public static final String APPLICATION_PREFIX = "dev.get${artifactId}s.${artifactId}s.app.serve.prefix";

    public static final String OPENAPI_TAG = "Example";

    /**
     * Here we register all the classes that we want to be included in the Element.
     */
    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(
                //Endpoints
                HelloWorld.class,
                HelloWithAuthentication.class,
                ExampleContent.class,

                // Exposes the default security rules for the API. Assumes you are using the builtin Elements auth
                // system by setting `dev.get${artifactId}s.${artifactId}s.auth.enabled` to true in the annotation above.
                OpenAPISecurityConfig.class

        );
    }

}
