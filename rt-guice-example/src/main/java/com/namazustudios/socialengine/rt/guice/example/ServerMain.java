package com.namazustudios.socialengine.rt.guice.example;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.namazustudios.socialengine.rt.Constants;
import com.namazustudios.socialengine.rt.guice.EdgeFilterListModule;
import com.namazustudios.socialengine.rt.guice.SimpleServerModule;
import com.namazustudios.socialengine.rt.lua.guice.ClasspathScanningLuaResourceModule;
import com.namazustudios.socialengine.rt.mina.guice.MinaServerModule;
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
                new ClasspathScanningLuaResourceModule() {
                    @Override
                    protected void configure() {
                        scanForEdgeResources("server.edge");
                        scanForInternalResources("server.internal");
                    }
                },
                new EdgeFilterListModule() {
                    @Override
                    protected void configureFilters() {}
                }
        );

        final SocketAcceptor socketAcceptor = injector.getInstance(SocketAcceptor.class);
        socketAcceptor.bind(new InetSocketAddress(Constants.DEFAULT_PORT));

    }

}
