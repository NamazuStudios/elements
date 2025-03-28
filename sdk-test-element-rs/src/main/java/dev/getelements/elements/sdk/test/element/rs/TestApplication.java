package dev.getelements.elements.sdk.test.element.rs;

import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import dev.getelements.elements.sdk.ElementLoader;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.ElementRegistrySupplier;
import dev.getelements.elements.sdk.Event;
import dev.getelements.elements.sdk.annotation.ElementDefaultAttribute;
import dev.getelements.elements.sdk.annotation.ElementEventConsumer;
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

    @ElementDefaultAttribute("myapp")
    public static final String APP_SERVE_PREFIX = "dev.getelements.elements.app.serve.prefix";

    @Override
    public Set<Class<?>> getClasses() {

        final var registry = ElementRegistrySupplier.getElementLocal(getClass()).get();
        logger.info("Using ElementRegistry {}", registry);

        return Set.of(
                MessageEndpoint.class,
                JacksonJsonProvider.class
        );

    }

}
