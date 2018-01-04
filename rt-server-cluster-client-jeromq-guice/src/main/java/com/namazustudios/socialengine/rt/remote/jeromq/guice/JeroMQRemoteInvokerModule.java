package com.namazustudios.socialengine.rt.remote.jeromq.guice;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.namazustudios.socialengine.remote.jeromq.JeroMQRemoteInvoker;
import com.namazustudios.socialengine.rt.jackson.guice.ObjectMapperPayloadReaderWriterModule;
import com.namazustudios.socialengine.rt.jeromq.DynamicConnectionPool;
import com.namazustudios.socialengine.rt.jeromq.ConnectionPool;
import com.namazustudios.socialengine.rt.remote.RemoteInvoker;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping.NON_FINAL;
import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.remote.jeromq.JeroMQRemoteInvoker.CONNECT_ADDRESS;

public class JeroMQRemoteInvokerModule extends PrivateModule {

    private Runnable bindConnectAddressAction = () -> {};

    /**
     * Specifies the connect address used by the underlying {@link JeroMQRemoteInvoker}.  This provides a binding for
     * the option {@link JeroMQRemoteInvoker#CONNECT_ADDRESS}.
     *
     * @param connectAddress the connect address
     * @return this instance
     */
    public JeroMQRemoteInvokerModule withConnectAddress(final String connectAddress) {
        bindConnectAddressAction = () -> bind(String.class)
            .annotatedWith(named(CONNECT_ADDRESS))
            .toInstance(connectAddress);
        return this;
    }

    @Override
    protected void configure() {

        install(new ObjectMapperPayloadReaderWriterModule());
        bindConnectAddressAction.run();

        bind(RemoteInvoker.class).to(JeroMQRemoteInvoker.class);
        bind(ConnectionPool.class).to(DynamicConnectionPool.class).asEagerSingleton();

        expose(RemoteInvoker.class);

    }

    @Provides
    @Singleton
    public ObjectMapper objectMapper(final CBORFactory cborFactory) {
        final ObjectMapper objectMapper = new ObjectMapper(cborFactory);
        objectMapper.enableDefaultTyping();
        objectMapper.enableDefaultTyping(NON_FINAL);
        objectMapper.disable(FAIL_ON_UNKNOWN_PROPERTIES);
        return objectMapper;
    }

}
