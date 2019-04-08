package com.namazustudios.socialengine.rt.remote.jeromq.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.namazustudios.socialengine.remote.jeromq.JeroMQMultiplexedConnectionsManager;
import com.namazustudios.socialengine.rt.MultiplexedConnectionsManager;
import org.testng.annotations.Test;
import org.zeromq.ZContext;

import static com.google.inject.name.Names.named;
import static java.util.UUID.randomUUID;

public class JeroMQMultiplexerTest {

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testFailsWithBadHostname() {

        final ZContext zContext = new ZContext();

        final MultiplexedConnectionsManager mux = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {

                bind(ZContext.class).toInstance(zContext);

                bind(String.class)
                    .annotatedWith(named(JeroMQMultiplexedConnectionsManager.CONNECT_ADDR))
                    .toInstance("tcp://" + randomUUID() + ":28883");

                bind(MultiplexedConnectionsManager.class)
                    .to(JeroMQMultiplexedConnectionsManager.class)
                    .asEagerSingleton();

            }
        }).getInstance(MultiplexedConnectionsManager.class);

        mux.start();

    }

}
