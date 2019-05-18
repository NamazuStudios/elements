package com.namazustudios.socialengine.appserve.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.remote.jeromq.JeroMQConnectionMultiplexer;
import com.namazustudios.socialengine.rt.ConnectionMultiplexer;
import org.zeromq.ZContext;

public class JeroMQMultiplexerModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ConnectionMultiplexer.class).to(JeroMQConnectionMultiplexer.class).asEagerSingleton();
    }

}
