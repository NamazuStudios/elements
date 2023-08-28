package dev.getelements.elements.docserve;

import com.google.inject.Injector;
import dev.getelements.elements.rest.support.DefaultExceptionMapper;
import dev.getelements.elements.rest.support.ISODateParamConverter;
import dev.getelements.elements.rest.jersey.swagger.EnhancedApiListingResource;
import dev.getelements.elements.rt.exception.InternalException;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.moxy.json.MoxyJsonConfig;
import org.glassfish.jersey.server.ResourceConfig;
import org.jvnet.hk2.guice.bridge.api.GuiceBridge;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.ServletContext;

public class DocGuiceResourceConfig extends ResourceConfig {

    private static final Logger logger = LoggerFactory.getLogger(DocGuiceResourceConfig.class);

    public static final String INJECTOR_ATTRIBUTE_NAME = DocGuiceResourceConfig.class.getName() + ".Injector";
    @Inject
    public DocGuiceResourceConfig(final ServiceLocator serviceLocator, final ServletContext context) {

        register(SwaggerSerializers.class);
        register(ISODateParamConverter.class);
        register(DefaultExceptionMapper.class);
        register(EnhancedApiListingResource.class);

        packages(true, "dev.getelements.elements.docserve.api");

        if (!tryConfigureJackson() || !tryConfigureMoxy()) {
            logger.warn("Neither Jackson nor Moxy could be configured.  Using default media support.");
        }

        GuiceBridge.getGuiceBridge().initializeGuiceBridge(serviceLocator);

        final var guiceBridge = serviceLocator.getService(GuiceIntoHK2Bridge.class);
        final var injector = (Injector) context.getAttribute(INJECTOR_ATTRIBUTE_NAME);
        guiceBridge.bridgeGuiceInjector(injector);

    }

    private boolean tryConfigureJackson() {
        try {

            // This attempts to soft-load Jackson support.  The jersey-media-moxy dependency
            // would be ideal.  However, it currently chokes on some of the generics stuff
            // we're using in our data model.  DocumentEntry don't want to make this a hard dependency
            // so we safely try to load it from the classpath and log a warning if that fails.

            final Class<?> cls = Class.forName("org.glassfish.jersey.jackson.JacksonFeature");
            register(cls);
            logger.info("Using Jackson Support.");

            return true;

        } catch (ClassNotFoundException ex) {
            logger.info("Jackson not found.  Skipping support.");
            return false;
        }
    }

    private boolean tryConfigureMoxy() {
        try {

            final Class<?> cls =  Class.forName("org.glassfish.jersey.moxy.json.MoxyJsonFeature");
            logger.info("Found MOXy support {}", cls);

            final Class<?> beanValidationMode = Class.forName("org.eclipse.persistence.jaxb.BeanValidationMode");

            register(new MoxyJsonConfig()
                .property("eclipselink.beanvalidation.mode", beanValidationMode.getField("NONE").get(null))
                .resolver());

            return true;
        } catch (ClassNotFoundException ex) {
            logger.info("MOXy Not Found.  Skipping support.");
            return false;
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            logger.error("Failed to disable bean validation in MOXy.", ex);
            throw new InternalException(ex);
        }
    }


}
