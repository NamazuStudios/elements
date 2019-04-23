package com.namazustudios.socialengine.rt.remote.jeromq.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.namazustudios.socialengine.remote.jeromq.JeroMQMultiplexedConnectionService;
import com.namazustudios.socialengine.rt.remote.ConnectionService;
import org.testng.annotations.Test;
import org.zeromq.ZContext;

public class JeroMQMultiplexerTest {

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testFailsWithBadHostname() {

        final ZContext zContext = new ZContext();

        final ConnectionService mux = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {

                bind(ZContext.class).toInstance(zContext);

                bind(ConnectionService.class)
                    .to(JeroMQMultiplexedConnectionService.class)
                    .asEagerSingleton();

            }
        }).getInstance(ConnectionService.class);

        mux.start();

    }

}
