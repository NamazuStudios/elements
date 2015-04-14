package com.namazustudios.promotion.rest.guice;

import com.google.inject.Injector;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.ResourceConfig;
import org.jvnet.hk2.guice.bridge.api.GuiceBridge;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Feature;

/**
 * Created by patricktwohig on 3/20/15.
 */
public class GuiceResourceConfig extends ResourceConfig {

    private static final Logger LOG = LoggerFactory.getLogger(GuiceResourceConfig.class);

    public static final String INJECOR_ATTRIBUTE_NAME = GuiceResourceConfig.class.getName() + ".Injector";

    @Inject
    public GuiceResourceConfig(ServiceLocator serviceLocator, ServletContext context) {

        packages(true, "com.namazustudios.promotion.rest");
        packages(true, "com.namazustudios.promotion.model");

        try {
            final Class<?> cls = getClass().forName("org.glassfish.jersey.jackson.JacksonFeature");
            register(cls);
        } catch (ClassNotFoundException ex) {
            LOG.info("NOT loading Jackson support.");
        }

        GuiceBridge.getGuiceBridge().initializeGuiceBridge(serviceLocator);

        final GuiceIntoHK2Bridge guiceBridge = serviceLocator.getService(GuiceIntoHK2Bridge.class);
        final Injector injector = (Injector) context.getAttribute(INJECOR_ATTRIBUTE_NAME);
        guiceBridge.bridgeGuiceInjector(injector);

    }

}
