package com.namazustudios.socialengine.rt.remote.jeromq.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.namazustudios.socialengine.remote.jeromq.JeroMQMultiplexedConnectionService;
import com.namazustudios.socialengine.rt.remote.ConnectionService;
import com.namazustudios.socialengine.rt.remote.srv.SpotifySrvMonitor;
import com.namazustudios.socialengine.rt.remote.srv.SrvMonitor;
import org.testng.annotations.Test;
import org.zeromq.ZContext;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.remote.jeromq.JeroMQMultiplexedConnectionService.APPLICATION_NODE_FQDN;

public class JeroMQMultiplexerTest {

//    @Test(expectedExceptions = IllegalArgumentException.class)
//    public void testFailsWithBadHostname() {
//
//        final ZContext zContext = new ZContext();
//
//        final ConnectionService mux = Guice.createInjector(new AbstractModule() {
//            @Override
//            protected void configure() {
//
//                bind(String.class).annotatedWith(named(APPLICATION_NODE_FQDN)).toInstance("appnode.tcp.namazustudios.com.");
//
//                bind(ZContext.class).toInstance(zContext);
//
//                bind(ConnectionService.class)
//                    .to(JeroMQMultiplexedConnectionService.class)
//                    .asEagerSingleton();
//
//                bind(SrvMonitor.class)
//                        .to(SpotifySrvMonitor.class)
//                        .asEagerSingleton();
//
//            }
//        }).getInstance(ConnectionService.class);
//
//        mux.start();
//
//    }

}
