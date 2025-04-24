package dev.getelements.elements.rest.guice;

import com.google.inject.Injector;
import dev.getelements.elements.rest.mission.ProgressResource;
import dev.getelements.elements.rt.exception.InternalException;
import dev.getelements.elements.rt.jersey.CsvFeature;
import io.swagger.v3.jaxrs2.SwaggerSerializers;
import jakarta.inject.Inject;
import jakarta.servlet.ServletContext;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.moxy.json.MoxyJsonConfig;
import org.glassfish.jersey.server.ResourceConfig;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static dev.getelements.elements.guice.GuiceConstants.GUICE_INJECTOR_ATTRIBUTE_NAME;
import static org.jvnet.hk2.guice.bridge.api.GuiceBridge.getGuiceBridge;

/**
 * Created by patricktwohig on 3/20/15.
 *
 */
public class RestAPIGuiceResourceConfig extends ResourceConfig {

    private static final Logger logger = LoggerFactory.getLogger(RestAPIGuiceResourceConfig.class);

    @Inject
    public RestAPIGuiceResourceConfig(final ServiceLocator serviceLocator, final ServletContext context) {

        register(CsvFeature.class);
        register(SwaggerSerializers.class);
        register(ProgressResource.class);

        packages(true, "dev.getelements.elements.rest");
        packages(true, "dev.getelements.elements.model");
        packages(true, "dev.getelements.elements.sdk.jakarta.rs");

        if (!tryConfigureJackson() || !tryConfigureMoxy()) {
            logger.warn("Neither Jackson nor Moxy could be configured.  Using default media support.");
        }

        tryConfigureMultipart();

        getGuiceBridge().initializeGuiceBridge(serviceLocator);

        final GuiceIntoHK2Bridge guiceBridge = serviceLocator.getService(GuiceIntoHK2Bridge.class);
        final Injector injector = (Injector) context.getAttribute(GUICE_INJECTOR_ATTRIBUTE_NAME);
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

    private boolean tryConfigureMultipart() {
        try {

            final Class<?> nativeMultipart = Class.forName("org.glassfish.jersey.media.multipart.MultiPartFeature");
            logger.info("Found Native Multipart support {}", nativeMultipart);

            final Class<?> genericMultipart = Class.forName("dev.getelements.elements.rt.jersey.GenericMultipartFeature");
            register(nativeMultipart);
            register(genericMultipart);

            return true;
        } catch (ClassNotFoundException ex) {
            logger.info("Multipart support not found.  Skipping support.");
            return false;
        }
    }

}
