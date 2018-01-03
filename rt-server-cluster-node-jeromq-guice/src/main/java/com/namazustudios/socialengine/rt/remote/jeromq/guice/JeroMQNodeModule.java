package com.namazustudios.socialengine.rt.remote.jeromq.guice;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.namazustudios.socialengine.remote.jeromq.JeroMQNode;
import com.namazustudios.socialengine.rt.Node;
import com.namazustudios.socialengine.rt.jackson.guice.ObjectMapperPayloadReaderWriterModule;
import com.namazustudios.socialengine.rt.jeromq.DynamicConnectionPool;
import com.namazustudios.socialengine.rt.jeromq.ConnectionPool;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES;
import static com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping.NON_FINAL;
import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.remote.jeromq.JeroMQNode.BIND_ADDRESS;

public class JeroMQNodeModule extends PrivateModule {

    private String bindAddress = null;

    /**
     * Specifies the bind address using the {@link JeroMQNode#BIND_ADDRESS} name.
     *
     * @param bindAddress the bind address
     * @return this instance
     */
    public JeroMQNodeModule withBindAddress(final String bindAddress) {
        this.bindAddress = bindAddress;
        return this;
    }

    @Override
    protected void configure() {

        install(new ObjectMapperPayloadReaderWriterModule());

        bind(Node.class).to(JeroMQNode.class).asEagerSingleton();
        bind(ConnectionPool.class).to(DynamicConnectionPool.class);

        if (bindAddress != null) {
            bind(String.class).annotatedWith(named(BIND_ADDRESS)).toInstance(bindAddress);
        }

        expose(Node.class);

    }

    @Provides
    @Singleton
    public ObjectMapper objectMapper(final CBORFactory cborFactory) {
        final ObjectMapper objectMapper = new ObjectMapper(cborFactory);
        objectMapper.enableDefaultTyping();
        objectMapper.enableDefaultTyping(NON_FINAL);
        objectMapper.disable(FAIL_ON_IGNORED_PROPERTIES);
        return objectMapper;
    }

}
