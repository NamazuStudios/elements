package com.namazustudios.socialengine.rt.remote.jeromq.guice;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.remote.jeromq.JeroMQNode;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.jeromq.ConnectionPool;
import com.namazustudios.socialengine.rt.jeromq.SimpleConnectionPool;
import com.namazustudios.socialengine.rt.remote.Node;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.remote.jeromq.JeroMQNode.NAME;

public class JeroMQNodeModule extends PrivateModule {

    private Runnable bindNodeIdAction = () -> {};

    private Runnable bindNodeNameAction = () -> {};

    private Runnable bindMinConnectionsAction = () -> {};

    private Runnable bindMaxConnectionsAction = () -> {};

    private Runnable bindTimeoutAction = () -> {};

    /**
     * Specifies the node unique id.
     *
     * @param nodeId the node ID
     * @return this instance
     */
    public JeroMQNodeModule withNodeId(final NodeId nodeId) {
        bindNodeIdAction = () -> bind(NodeId.class).toInstance(nodeId);
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
     * Specifies the connection timeout.  If a connection isn't used for the specified period of time, the underlying
     * connection is terminated and removed.  Leaving this unspecified will not assign any properties and leave it to
     * external means to configure the underlying module.
     *
     * @param timeoutInSeconds the timeout value, in seconds.
     * @return this instance
     */
    public JeroMQNodeModule withTimeout(final int timeoutInSeconds) {
        bindTimeoutAction = () -> bind(Integer.class)
                .annotatedWith(named(ConnectionPool.TIMEOUT))
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
                .annotatedWith(named(ConnectionPool.MIN_CONNECTIONS))
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
                .annotatedWith(named(ConnectionPool.MAX_CONNECTIONS))
                .toInstance(maximumConnections);
        return this;
    }

    @Override
    protected void configure() {

        bind(Node.class).to(JeroMQNode.class).asEagerSingleton();
        bind(ConnectionPool.class).to(SimpleConnectionPool.class);

        bindNodeIdAction.run();
        bindNodeNameAction.run();
        bindTimeoutAction.run();
        bindMinConnectionsAction.run();
        bindMaxConnectionsAction.run();

        expose(Node.class);

    }

}
