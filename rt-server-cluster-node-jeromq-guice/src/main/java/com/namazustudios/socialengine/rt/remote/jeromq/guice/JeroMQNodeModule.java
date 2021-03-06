package com.namazustudios.socialengine.rt.remote.jeromq.guice;

import com.google.inject.PrivateModule;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.namazustudios.socialengine.remote.jeromq.JeroMQNode;
import com.namazustudios.socialengine.rt.id.ApplicationId;
import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.remote.Node;

import javax.inject.Provider;

import java.lang.annotation.Annotation;
import java.util.function.Function;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.remote.jeromq.JeroMQNode.*;
import static com.namazustudios.socialengine.rt.id.NodeId.forInstanceAndApplication;
import static com.namazustudios.socialengine.rt.id.NodeId.forMasterNode;
import static com.namazustudios.socialengine.rt.remote.Node.MASTER_NODE_NAME;

public class JeroMQNodeModule extends PrivateModule {

    private Runnable bindNodeIdAction = () -> {};

    private Runnable bindNodeNameAction = () -> {};

    private Runnable bindMinConnectionsAction = () -> {};

    private Runnable bindMaxConnectionsAction = () -> {};

    private Function<AnnotatedBindingBuilder<Node>, LinkedBindingBuilder<Node>> bindNodeAction = a -> a;

    private Runnable exposeNodeAction = () -> expose(Node.class);

    /**
     * Specifies the symbolic name of the node using the {@link JeroMQNode#NAME}.
     *
     * @param nodeId the Node ID
     * @return this instance
     */
    public JeroMQNodeModule withNodeId(final NodeId nodeId) {
        bindNodeIdAction = () -> bind(NodeId.class).toInstance(nodeId);
        return this;
    }

    /**
     * A shortcut for invoking {@link #withNodeId(InstanceId, ApplicationId)} using
     * {@link NodeId#forInstanceAndApplication(InstanceId, ApplicationId)}.
     *
     * @param instanceId the {@link InstanceId}
     * @param applicationId the {@link ApplicationId}
     * @return this instance
     */
    public JeroMQNodeModule withNodeId(final InstanceId instanceId, final ApplicationId applicationId) {
        return withNodeId(forInstanceAndApplication(instanceId, applicationId));
    }

    /**
     * Specifies the default symbolic name of the node using the {@link JeroMQNode#NAME}.
     *
     * @return this instance
     */
    public JeroMQNodeModule withDefaultNodeName() {

        bindNodeNameAction = () -> {
            final Provider<NodeId> nodeIdProvider = getProvider(NodeId.class);
            bind(String.class)
                .annotatedWith(named(NAME))
                .toProvider(() -> nodeIdProvider.get().asString());
        };

        return this;

    }

    /**
     * Specifies the symbolic name of the node using the {@link JeroMQNode#NAME}.
     *
     * @param nodeName the node name
     * @return this instance
     */
    public JeroMQNodeModule withNodeName(final String nodeName) {
        bindNodeNameAction = () -> bind(String.class).annotatedWith(named(NAME)).toInstance(nodeName);
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
            .annotatedWith(named(JEROMQ_NODE_MIN_CONNECTIONS))
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
            .annotatedWith(named(JEROMQ_NODE_MAX_CONNECTIONS))
            .toInstance(maximumConnections);
        return this;
    }

    /**
     * Specifies an {@link Annotation} to bind to the underlying {@link Node}.
     *
     * @param annotation the literal annotation to bind
     * @return this instance
     */
    public JeroMQNodeModule withAnnotation(final Annotation annotation) {
        bindNodeAction = a -> a.annotatedWith(annotation);
        exposeNodeAction = () -> expose(Node.class).annotatedWith(annotation);
        return this;
    }

    /**
     * Specifies the annotation as a {@link @Name} annotated node.
     *
     * @param name the name
     * @return this instance
     */
    public JeroMQNodeModule withAnnotation(final String name) {
        return withAnnotation(named(name));
    }

    /**
     * Indicates that this instance should bind the {@link Node} as a master node supplying
     * the {@link InstanceId}
     *
     * @param instanceId the {@link InstanceId}
     * @return this instance
     */
    public JeroMQNodeModule withMasterNodeForInstanceId(final InstanceId instanceId) {
        withNodeId(forMasterNode(instanceId));
        withAnnotation(named(MASTER_NODE_NAME));
        return this;
    }

    @Override
    protected void configure() {

        bindNodeAction.apply(bind(Node.class)).to(JeroMQNode.class).asEagerSingleton();

        bindNodeIdAction.run();
        bindNodeNameAction.run();
        bindMinConnectionsAction.run();
        bindMaxConnectionsAction.run();

        exposeNodeAction.run();

    }

}
