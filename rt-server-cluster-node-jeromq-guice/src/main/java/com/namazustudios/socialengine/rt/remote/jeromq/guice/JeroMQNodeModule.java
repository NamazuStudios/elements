package com.namazustudios.socialengine.rt.remote.jeromq.guice;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.remote.jeromq.JeroMQNode;
import com.namazustudios.socialengine.rt.Node;
import com.namazustudios.socialengine.rt.fst.FSTPayloadReaderWriterModule;
import com.namazustudios.socialengine.rt.jeromq.ConnectionPool;
import com.namazustudios.socialengine.rt.jeromq.DynamicConnectionPool;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.remote.jeromq.JeroMQNode.*;
import static com.namazustudios.socialengine.rt.jeromq.DynamicConnectionPool.MAX_CONNECTIONS;
import static com.namazustudios.socialengine.rt.jeromq.DynamicConnectionPool.MIN_CONNECTIONS;
import static com.namazustudios.socialengine.rt.jeromq.DynamicConnectionPool.TIMEOUT;

public class JeroMQNodeModule extends PrivateModule {

    private Runnable bindAddressAction = () -> {};

    private Runnable bindNodeIdAction = () -> {};

    private Runnable bindNodeNameAction = () -> {};

    private Runnable bindMinConnectionsAction = () -> {};

    private Runnable bindMaxConnectionsAction = () -> {};

    private Runnable bindTimeoutAction = () -> {};

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
     * @param minimumConnections the minimum number of connections to keep issueOpenInprocChannelCommand
     * @return this instance
     */
    public JeroMQNodeModule withMinimumConnections(final int minimumConnections) {
        bindMinConnectionsAction = () -> bind(Integer.class)
                .annotatedWith(named(MIN_CONNECTIONS))
                .toInstance(minimumConnections);
        return this;
    }

    /**
     * Specifies the maximum number of connections to keep active, even if the timeout has expired.
     *
     * @param maximumConnections the minimum number of connections to keep issueOpenInprocChannelCommand
     * @return this instance
     */
    public JeroMQNodeModule withMaximumConnections(int maximumConnections) {
        bindMaxConnectionsAction = () -> bind(Integer.class)
                .annotatedWith(named(MAX_CONNECTIONS))
                .toInstance(maximumConnections);
        return this;
    }

    @Override
    protected void configure() {

        install(new FSTPayloadReaderWriterModule());

        bind(Node.class).to(JeroMQNode.class).asEagerSingleton();
        bind(ConnectionPool.class).to(DynamicConnectionPool.class);

        bindNodeIdAction.run();
        bindNodeNameAction.run();
        bindAddressAction.run();
        bindTimeoutAction.run();
        bindMinConnectionsAction.run();
        bindMaxConnectionsAction.run();

        expose(Node.class);

    }
}
