package com.namazustudios.socialengine.rt.lua.guice;

import com.google.inject.Module;
import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.guice.*;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.remote.InvocationDispatcher;
import com.namazustudios.socialengine.rt.remote.IoCInvocationDispatcher;
import com.namazustudios.socialengine.rt.remote.Node;
import com.namazustudios.socialengine.rt.remote.NodeLifecycle;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.ContextNodeLifecycle;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.JeroMQNodeModule;
import org.zeromq.ZContext;

import java.util.ArrayList;
import java.util.List;

public class TestJeroMQNodeModule extends PrivateModule {

    private Runnable handlerTimeoutBindAction = () -> {};

    private Runnable contextBindAction = () -> bind(ZContext.class).asEagerSingleton();

    private List<Module> nodeModules = new ArrayList<>();

    private final JeroMQNodeModule jeroMQNodeModule = new JeroMQNodeModule();

    public TestJeroMQNodeModule withNodeModules(final List<Module> nodeModules) {
        this.nodeModules.addAll(nodeModules);
        return this;
    }

    @Override
    protected void configure() {

        expose(Node.class);
        expose(Context.class);

        contextBindAction.run();
        handlerTimeoutBindAction.run();

        bind(IoCInvocationDispatcher.class).asEagerSingleton();
        bind(InvocationDispatcher.class).to(IoCInvocationDispatcher.class);
        bind(AssetLoader.class).toProvider(() -> new ClasspathAssetLoader(getClass().getClassLoader()));

        bind(ContextNodeLifecycle.class).asEagerSingleton();
        bind(NodeLifecycle.class).to(ContextNodeLifecycle.class);

        install(new GuiceIoCResolverModule());
        install(jeroMQNodeModule);
        nodeModules.forEach(this::install);

    }

    /**
     * Allows for the specification of a specific {@link ZContext} instance
     * @param zContext the {@link ZContext} instance.
     * @return this instance
     */
    public TestJeroMQNodeModule withZContext(final ZContext zContext) {
        contextBindAction = () -> bind(ZContext.class).toInstance(zContext);
        return this;
    }

    /**
     * {@see {@link JeroMQNodeModule#withNodeId(NodeId)}}
     *
     * @param nodeId the node Id
     * @return this instance
     */
    public TestJeroMQNodeModule withNodeId(final NodeId nodeId) {
        jeroMQNodeModule.withNodeId(nodeId);
        return this;
    }

    /**
     * {@see {@link JeroMQNodeModule#withNodeName(String)}}
     *
     * @param nodeName the node Id
     * @return this instance
     */
    public TestJeroMQNodeModule withNodeName(String nodeName) {
        jeroMQNodeModule.withNodeName(nodeName);
        return this;
    }

    /**
     * {@see {@link JeroMQNodeModule#withBindAddress(String)}}
     *
     * @param bindAddress the bind address
     * @return this instance
     */
    public TestJeroMQNodeModule withBindAddress(String bindAddress) {
        jeroMQNodeModule.withBindAddress(bindAddress);
        return this;
    }

    /**
     * {@see {@link JeroMQNodeModule#withTimeout(int)}}
     *
     * @param timeoutInSeconds the timeout, in seconds
     * @return this instance
     */
    public TestJeroMQNodeModule withTimeout(int timeoutInSeconds) {
        jeroMQNodeModule.withTimeout(timeoutInSeconds);
        return this;
    }

    /**
     * {@see {@link JeroMQNodeModule#withMinimumConnections(int)}}
     *
     * @param minimumConnections the minimum number of connections to keep in each connection pool
     * @return this instance
     */
    public TestJeroMQNodeModule withMinimumConnections(int minimumConnections) {
        jeroMQNodeModule.withMinimumConnections(minimumConnections);
        return this;
    }

    /**
     * {@see {@link JeroMQNodeModule#withMaximumConnections(int)}}
     *
     * @param maximumConnections the minimum number of connections to keep in each connection pool
     * @return this instance
     */
    public TestJeroMQNodeModule withMaximumConnections(int maximumConnections) {
        jeroMQNodeModule.withMaximumConnections(maximumConnections);
        return this;
    }

}
