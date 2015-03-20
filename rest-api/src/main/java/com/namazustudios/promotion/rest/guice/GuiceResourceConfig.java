package com.namazustudios.promotion.rest.guice;

import com.google.inject.Injector;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.ResourceConfig;
import org.jvnet.hk2.guice.bridge.api.GuiceBridge;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;

import javax.inject.Inject;
import javax.servlet.ServletContext;

/**
 * Created by patricktwohig on 3/20/15.
 */
public class GuiceResourceConfig extends ResourceConfig {

    @Inject
    public GuiceResourceConfig(ServiceLocator serviceLocator, ServletContext context) {


        packages(true, "com.namazustudios.promotion.rest");

        GuiceBridge.getGuiceBridge().initializeGuiceBridge(serviceLocator);

        final GuiceIntoHK2Bridge guiceBridge = serviceLocator.getService(GuiceIntoHK2Bridge.class);
        final Injector injector = (Injector) context.getAttribute(GuiceMain.INJECOR_ATTRIBUTE_NAME);
        guiceBridge.bridgeGuiceInjector(injector);

    }
}
