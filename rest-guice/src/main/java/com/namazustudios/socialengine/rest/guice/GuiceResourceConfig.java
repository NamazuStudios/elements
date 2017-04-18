package com.namazustudios.socialengine.rest.guice;

import com.google.inject.Injector;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.ResourceConfig;
import org.jvnet.hk2.guice.bridge.api.GuiceBridge;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.ServletContext;

/**
 * Created by patricktwohig on 3/20/15.
 *
 */
public class GuiceResourceConfig extends ResourceConfig {

    private static final Logger logger = LoggerFactory.getLogger(GuiceResourceConfig.class);

    public static final String INJECOR_ATTRIBUTE_NAME = GuiceResourceConfig.class.getName() + ".Injector";

    @Inject
    public GuiceResourceConfig(ServiceLocator serviceLocator, ServletContext context) {

        packages(true, "io.swagger.jaxrs.listing");
        packages(true, "com.namazustudios.socialengine.rest");
        packages(true, "com.namazustudios.socialengine.model");

        try {

            // This attempts to soft-load Jackson support.  The jersey-media-moxy dependency
            // would be ideal.  However, it currently chokes on some of the generics stuff
            // we're using in our data model.  DocumentEntry don't want to make this a hard dependency
            // so we safely try to load it from the classpath and log a warning if that fails.

            final Class<?> cls = getClass().forName("org.glassfish.jersey.jackson.JacksonFeature");
            register(cls);

        } catch (ClassNotFoundException ex) {
            logger.info("NOT loading Jackson support.");
        }

        GuiceBridge.getGuiceBridge().initializeGuiceBridge(serviceLocator);

        final GuiceIntoHK2Bridge guiceBridge = serviceLocator.getService(GuiceIntoHK2Bridge.class);
        final Injector injector = (Injector) context.getAttribute(INJECOR_ATTRIBUTE_NAME);
        guiceBridge.bridgeGuiceInjector(injector);

    }

}
