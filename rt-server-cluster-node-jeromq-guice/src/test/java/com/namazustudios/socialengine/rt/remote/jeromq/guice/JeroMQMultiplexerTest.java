package com.namazustudios.socialengine.rt.remote.jeromq.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.namazustudios.socialengine.remote.jeromq.JeroMQConnectionMultiplexer;
import com.namazustudios.socialengine.rt.ConnectionMultiplexer;
import org.testng.annotations.Test;
import org.zeromq.ZContext;

import static com.google.inject.name.Names.named;
import static java.util.UUID.randomUUID;

public class JeroMQMultiplexerTest {

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testFailsWithBadHostname() {

        final ZContext zContext = new ZContext();

        final ConnectionMultiplexer mux = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {

                bind(ZContext.class).toInstance(zContext);

                bind(String.class)
                    .annotatedWith(named(JeroMQConnectionMultiplexer.CONNECT_ADDR))
                    .toInstance("tcp://" + randomUUID() + ":28883");

                bind(ConnectionMultiplexer.class)
                    .to(JeroMQConnectionMultiplexer.class)
                    .asEagerSingleton();

            }
        }).getInstance(ConnectionMultiplexer.class);

        mux.start();

    }

}
