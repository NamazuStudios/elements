#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import ${package}.rest.ExampleContent;
import ${package}.rest.HelloWithAuthentication;
import ${package}.rest.HelloWorld;
import dev.getelements.elements.sdk.annotation.ElementDefaultAttribute;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.annotation.ElementServiceImplementation;
import jakarta.ws.rs.core.Application;

import java.util.Set;

@ElementServiceImplementation
@ElementServiceExport(Application.class)
public class HelloWorldApplication extends Application {

    @ElementDefaultAttribute("true")
    public static final String AUTH_ENABLED = "dev.getelements.elements.auth.enabled";

    @ElementDefaultAttribute("/element/example/rest/api")
    public static final String RS_ROOT = "dev.getelements.elements.element.rs.root";

    @ElementDefaultAttribute("/element/example/ws")
    public static final String WS_ROOT = "dev.getelements.elements.element.ws.root";

    @ElementDefaultAttribute("/app/static/test/path")
    public static final String STATIC_CONTENT_URI = "dev.getelements.element.static.uri";

    @ElementDefaultAttribute("/app/ui/test/path")
    public static final String UI_CONTENT_URI = "dev.getelements.element.ui.uri";

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
                // system by setting `dev.getelements.elements.auth.enabled` to true in the annotation above.
                OpenAPISecurityConfig.class

        );
    }

}
