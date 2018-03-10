package com.namazustudios.socialengine.rt.lua.guice;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.guice.*;
import com.namazustudios.socialengine.rt.remote.InvocationDispatcher;
import com.namazustudios.socialengine.rt.remote.IoCInvocationDispatcher;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.JeroMQNodeModule;
import org.zeromq.ZContext;

public class TestJeroMQLuaNodeModule extends PrivateModule {

    private final JeroMQNodeModule jeroMQNodeModule = new JeroMQNodeModule();

    private Runnable contextBindAction = () -> bind(ZContext.class).asEagerSingleton();

    @Override
    protected void configure() {

        expose(Node.class);

        contextBindAction.run();
        bind(InvocationDispatcher.class).to(ContextInvocationDispatcher.class).asEagerSingleton();
        bind(AssetLoader.class).toProvider(() -> new ClasspathAssetLoader(getClass().getClassLoader()));

        install(new LuaModule() {
            @Override
            protected void configureFeatures() {
                super.configureFeatures();
                bindBuiltin(TestJavaModule.class).toModuleNamed("test.java.module");
            }
        });

        bind(Context.class).to(SimpleContext.class).asEagerSingleton();
        install(new SimpleServicesModule());
        install(new SimpleResourceContextModule());
        install(new SimpleIndexContextModule());
        install(new SimpleSchedulerContextModule());
        install(new SimpleHandlerContextModule());
        install(new GuiceIoCResolverModule());
        install(jeroMQNodeModule);

    }

    /**
     * Allows for the specification of a specific {@link ZContext} instance
     * @param zContext the {@link ZContext} instance.
     * @return this instance
     */
    public TestJeroMQLuaNodeModule withZContext(final ZContext zContext) {
        contextBindAction = () -> bind(ZContext.class).toInstance(zContext);
        return this;
    }

    /**
     * {@see {@link JeroMQNodeModule#withNodeId(String)}}
     *
     * @param nodeId the node Id
     * @return this instance
     */
    public TestJeroMQLuaNodeModule withNodeId(String nodeId) {
        jeroMQNodeModule.withNodeId(nodeId);
        return this;
    }

    /**
     * {@see {@link JeroMQNodeModule#withNodeName(String)}}
     *
     * @param nodeName the node Id
     * @return this instance
     */
    public TestJeroMQLuaNodeModule withNodeName(String nodeName) {
        jeroMQNodeModule.withNodeName(nodeName);
        return this;
    }

    /**
     * {@see {@link JeroMQNodeModule#withBindAddress(String)}}
     *
     * @param bindAddress the bind address
     * @return this instance
     */
    public TestJeroMQLuaNodeModule withBindAddress(String bindAddress) {
        jeroMQNodeModule.withBindAddress(bindAddress);
        return this;
    }

    /**
     * {@see {@link JeroMQNodeModule#withTimeout(int)}}
     *
     * @param timeoutInSeconds the timeout, in seconds
     * @return this instance
     */
    public TestJeroMQLuaNodeModule withTimeout(int timeoutInSeconds) {
        jeroMQNodeModule.withTimeout(timeoutInSeconds);
        return this;
    }

    /**
     * {@see {@link JeroMQNodeModule#withMinimumConnections(int)}}
     *
     * @param minimumConnections the minimum number of connections to keep in each connection pool
     * @return this instance
     */
    public TestJeroMQLuaNodeModule withMinimumConnections(int minimumConnections) {
        jeroMQNodeModule.withMinimumConnections(minimumConnections);
        return this;
    }

    /**
     * {@see {@link JeroMQNodeModule#withMaximumConnections(int)}}
     *
     * @param maximumConnections the minimum number of connections to keep in each connection pool
     * @return this instance
     */
    public TestJeroMQLuaNodeModule withMaximumConnections(int maximumConnections) {
        jeroMQNodeModule.withMaximumConnections(maximumConnections);
        return this;
    }

    /**
     * {@see {@link JeroMQNodeModule#withNumberOfDispatchers(int)}}
     *
     * @param numberOfDispatchers the number of dispatcher threads for incoming requests
     * @return this instance
     */
    public TestJeroMQLuaNodeModule withNumberOfDispatchers(int numberOfDispatchers) {
        jeroMQNodeModule.withNumberOfDispatchers(numberOfDispatchers);
        return this;
    }

}
