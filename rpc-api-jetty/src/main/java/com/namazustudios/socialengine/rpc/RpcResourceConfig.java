package com.namazustudios.socialengine.rpc;

import com.google.inject.Injector;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.jvnet.hk2.guice.bridge.api.GuiceBridge;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;

import javax.inject.Inject;
import javax.servlet.ServletContext;

public class RpcResourceConfig extends ResourceConfig {

    public static final String INJECTOR_ATTRIBUTE_NAME = RpcResourceConfig.class.getName() + ".Injector";

    @Inject
    public RpcResourceConfig(final ServiceLocator serviceLocator, final ServletContext context) {

        register(JacksonFeature.class);
        packages(true, "com.namazustudios.socialengine.jrpc");
        packages(true, "com.namazustudios.socialengine.model");

        GuiceBridge.getGuiceBridge().initializeGuiceBridge(serviceLocator);

        final GuiceIntoHK2Bridge guiceBridge = serviceLocator.getService(GuiceIntoHK2Bridge.class);
        final Injector injector = (Injector) context.getAttribute(INJECTOR_ATTRIBUTE_NAME);
        guiceBridge.bridgeGuiceInjector(injector);

    }

}
