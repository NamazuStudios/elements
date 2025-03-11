package dev.getelements.elements.sdk.test.element.rs;

import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import dev.getelements.elements.sdk.annotation.ElementDefaultAttribute;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.annotation.ElementServiceImplementation;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

import java.util.Set;

@ApplicationPath("/")
@ElementServiceImplementation
@ElementServiceExport(Application.class)
public class TestApplication extends Application {

    @ElementDefaultAttribute("myapp")
    public static final String APP_SERVE_PREFIX = "dev.getelements.elements.app.serve.prefix";

    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(
                MessageEndpoint.class,
                JacksonJsonProvider.class
        );
    }

}
