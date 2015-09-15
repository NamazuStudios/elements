package com.namazustudios.socialengine.rt.guice.example;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.guice.EdgeFilterListModule;
import com.namazustudios.socialengine.rt.guice.ExceptionMapperModule;
import com.namazustudios.socialengine.rt.guice.SimpleServerModule;
import com.namazustudios.socialengine.rt.lua.FQNTypeRegistry;
import com.namazustudios.socialengine.rt.lua.TypeRegistry;
import com.namazustudios.socialengine.rt.lua.guice.ClasspathScanningLuaResourceModule;
import com.namazustudios.socialengine.rt.mina.guice.MinaServerModule;
import com.namazustudios.socialengine.rt.mina.guice.MinaSimpleServerContainerModule;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.transport.socket.SocketAcceptor;

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

                        scanForEdgeResources("server.edge");
                        scanForInternalResources("server.internal");

                        // We use fully-qualified type names, in this example, but this
                        // is heavily dependent upon how the backing scripts handle things
                        binder().bind(TypeRegistry.class).to(FQNTypeRegistry.class);

                    }
                },
                new EdgeFilterListModule() {
                    @Override
                    protected void configureFilters() {
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
