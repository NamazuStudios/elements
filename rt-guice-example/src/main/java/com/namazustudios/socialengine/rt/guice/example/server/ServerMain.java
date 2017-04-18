package com.namazustudios.socialengine.rt.guice.example.server;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.namazustudios.socialengine.rt.Constants;
import com.namazustudios.socialengine.rt.ServerContainer;
import com.namazustudios.socialengine.rt.guice.EdgeFilterListModule;
import com.namazustudios.socialengine.rt.guice.ExceptionMapperModule;
import com.namazustudios.socialengine.rt.guice.SimpleServerModule;
import com.namazustudios.socialengine.rt.lua.guice.ClasspathScanningLuaResourceModule;
import com.namazustudios.socialengine.rt.mina.guice.MinaServerModule;
import com.namazustudios.socialengine.rt.mina.guice.MinaSimpleServerContainerModule;

import java.net.InetSocketAddress;

/**
 * Created by patricktwohig on 9/1/15.
 */
public class ServerMain {

    public static void main(final String[] args) throws Exception {
        final Injector injector = Guice.createInjector(
                new SimpleServerModule(),
                new MinaServerModule(),
                new MinaSimpleServerContainerModule(),
                new ClasspathScanningLuaResourceModule() {
                    @Override
                    protected void configureResources() {
                        scanForEdgeResources("server.handler");
                        scanForInternalResources("server.worker");
                    }
                },
                new EdgeFilterListModule() {
                    @Override
                    protected void configureFilters() {
                        bindEdgeFilter().named("logging")
                                        .atBeginningOfFilterChain()
                                        .to(ServerLogFilter.class)
                                        .in(Scopes.SINGLETON);
                    }
                },
                new ExceptionMapperModule()
        );

        final ServerContainer serverContainer = injector.getInstance(ServerContainer.class);
        final ServerContainer.RunningInstance runningInstance = serverContainer.run(new InetSocketAddress(Constants.DEFAULT_PORT));

        try {
            runningInstance.waitForShutdown();
        } finally {
            runningInstance.shutdown();
        }

    }

}
