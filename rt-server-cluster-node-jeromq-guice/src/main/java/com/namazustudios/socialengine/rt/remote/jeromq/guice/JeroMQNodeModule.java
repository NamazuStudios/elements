package com.namazustudios.socialengine.rt.remote.jeromq.guice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.namazustudios.socialengine.remote.jeromq.JeroMQNode;
import com.namazustudios.socialengine.rt.Node;
import com.namazustudios.socialengine.rt.jackson.guice.ObjectMapperPayloadReaderWriterModule;
import com.namazustudios.socialengine.rt.jeromq.CachedConnectionPool;
import com.namazustudios.socialengine.rt.jeromq.ConnectionPool;

import static com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping.NON_FINAL;

public class JeroMQNodeModule extends PrivateModule {

    @Override
    protected void configure() {

        install(new ObjectMapperPayloadReaderWriterModule());

        bind(Node.class).to(JeroMQNode.class).asEagerSingleton();
        bind(ConnectionPool.class).to(CachedConnectionPool.class);

        expose(Node.class);

    }

    @Provides
    @Singleton
    public ObjectMapper objectMapper(final CBORFactory cborFactory) {
        final ObjectMapper objectMapper = new ObjectMapper(cborFactory);
        objectMapper.enableDefaultTyping();
        objectMapper.enableDefaultTyping(NON_FINAL);
        return objectMapper;
    }

}
