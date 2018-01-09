package com.namazustudios.socialengine.rt.remote.jeromq.guice;

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
import static com.namazustudios.socialengine.remote.jeromq.JeroMQNode.*;
import static com.namazustudios.socialengine.rt.jeromq.DynamicConnectionPool.MIN_CONNECTIONS;
import static com.namazustudios.socialengine.rt.jeromq.DynamicConnectionPool.TIMEOUT;

public class JeroMQNodeModule extends PrivateModule {

    private Runnable bindAddressAction = () -> {};

    private Runnable bindNodeIdAction = () -> {};

    private Runnable bindNodeNameAction = () -> {};

    private Runnable bindMinConnectionsAction = () -> {};

    private Runnable bindTimeoutAction = () -> {};

    private Runnable bindNumberOfDispatchersAction = () -> {};

    /**
     * Specifies the node unique id based {@link JeroMQNode#ID}.
     *
     * @param nodeId the node ID
     * @return this instance
     */
    public JeroMQNodeModule withNodeId(final String nodeId) {
        bindNodeIdAction = () -> bind(String.class).annotatedWith(named(ID)).toInstance(nodeId);
        return this;
    }

    /**
     * Specifies the node unique id based {@link JeroMQNode#NAME}.
     *
     * @param nodeName the node name
     * @return this instance
     */
    public JeroMQNodeModule withNodeName(final String nodeName) {
        bindNodeNameAction = () -> bind(String.class).annotatedWith(named(NAME)).toInstance(nodeName);
        return this;
    }

    /**
     * Specifies the bind address using the {@link JeroMQNode#BIND_ADDRESS} name.
     *
     * @param bindAddress the bind address
     * @return this instance
     */
    public JeroMQNodeModule withBindAddress(final String bindAddress) {
        bindAddressAction = () -> bind(String.class).annotatedWith(named(BIND_ADDRESS)).toInstance(bindAddress);
        return this;
    }

    /**
     * Specifies the connection timeout.  If a connection isn't used for the specified period of time, the underlying
     * connection is terminated and removed.  Leaving this unspecified will not assign any properties and leave it to
     * external means to configure the underlying module.
     *
     * @param timeoutInSeconds the timeout value, in seconds.
     * @return this instance
     */
    public JeroMQNodeModule withTimeout(final int timeoutInSeconds) {
        bindTimeoutAction = () -> bind(Integer.class)
                .annotatedWith(named(TIMEOUT))
                .toInstance(timeoutInSeconds);
        return this;
    }

    /**
     * Specifies the minimum number of connections to keep active, even if the timeout has expired.
     *
     * @param minimumConnections the minimum number of connections to keep open
     * @return this instance
     */
    public JeroMQNodeModule withMinimumConnections(final int minimumConnections) {
        bindMinConnectionsAction = () -> bind(Integer.class)
                .annotatedWith(named(MIN_CONNECTIONS))
                .toInstance(minimumConnections);
        return this;
    }

    /**
     * Specifies the number of dispatcher threads used to handle incoming connections.  The number of threads is fixed
     * for the underlying {@link JeroMQNode} instance.
     *
     * @param numberOfDispatchers the number of dispatchers to run.
     * @return this instance
     */
    public JeroMQNodeModule withNumberOfDispatchers(final int numberOfDispatchers) {
        bindNumberOfDispatchersAction = () -> bind(Integer.class)
                .annotatedWith(named(NUMBER_OF_DISPATCHERS))
                .toInstance(numberOfDispatchers);
        return this;
    }

    @Override
    protected void configure() {

        install(new ObjectMapperPayloadReaderWriterModule());

        bind(Node.class).to(JeroMQNode.class).asEagerSingleton();
        bind(ConnectionPool.class).to(DynamicConnectionPool.class);

        bindNodeIdAction.run();
        bindNodeNameAction.run();
        bindAddressAction.run();
        bindTimeoutAction.run();
        bindMinConnectionsAction.run();
        bindNumberOfDispatchersAction.run();

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
