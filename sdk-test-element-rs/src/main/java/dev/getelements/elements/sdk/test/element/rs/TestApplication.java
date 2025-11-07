package dev.getelements.elements.sdk.test.element.rs;

import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import dev.getelements.elements.sdk.ElementRegistrySupplier;
import dev.getelements.elements.sdk.ElementSupplier;
import dev.getelements.elements.sdk.annotation.ElementDefaultAttribute;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.annotation.ElementServiceImplementation;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

@ApplicationPath("/")
@ElementServiceImplementation
@ElementServiceExport(Application.class)
public class TestApplication extends Application {

    private static final Logger logger = LoggerFactory.getLogger(TestApplication.class);

    @ElementDefaultAttribute(value = "true", description = "Enabled by default for this Element.")
    public static final String AUTH_ENABLED = "dev.getelements.elements.auth.enabled";

    @ElementDefaultAttribute(value = "myapp", description = "The application serve prefix for this Element.")
    public static final String APP_SERVE_PREFIX = "dev.getelements.elements.app.serve.prefix";

    @Override
    public Set<Class<?>> getClasses() {

        // This should definitely be covered by a more comprehensive test elsewhere. However, this ensures that the
        // Element is able to gain access to its own registry and other instances.

        final var element = ElementSupplier.getElementLocal(getClass()).get();
        logger.info("Using Element {}", element);

        final var registry = ElementRegistrySupplier.getElementLocal(getClass()).get();
        logger.info("Using ElementRegistry {}", registry);

        return Set.of(
                MessageEndpoint.class,
                JacksonJsonProvider.class
        );

    }

}
