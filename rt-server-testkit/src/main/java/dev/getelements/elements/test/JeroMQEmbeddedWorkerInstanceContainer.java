package dev.getelements.elements.test;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import dev.getelements.elements.rt.IocResolver;
import dev.getelements.elements.rt.guice.SimpleExecutorsModule;
import dev.getelements.elements.rt.id.ApplicationId;
import dev.getelements.elements.rt.id.InstanceId;
import dev.getelements.elements.rt.id.NodeId;
import dev.getelements.elements.rt.jersey.JerseyHttpClientModule;
import dev.getelements.elements.rt.remote.Node;
import dev.getelements.elements.rt.remote.Worker;
import dev.getelements.elements.rt.remote.jeromq.guice.JeroMQInstanceConnectionServiceModule;
import dev.getelements.elements.test.guice.TestApplicationNodeModule;
import dev.getelements.elements.test.guice.TestMasterNodeModule;
import dev.getelements.elements.test.guice.TestWorkerInstanceModule;
import org.zeromq.ZContext;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.util.*;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

public class JeroMQEmbeddedWorkerInstanceContainer extends JeroMQEmbeddedInstanceContainer
                                                   implements EmbeddedWorkerInstanceContainer {

    private final List<Module> applicationModules = new ArrayList<>();

    private final Map<NodeId, Injector> applicationIdInjectorMap = new LinkedHashMap<>();

    private String bindAddress = format("inproc://%s.worker", randomUUID());

    private final JeroMQInstanceConnectionServiceModule jeroMQInstanceConnectionServiceModule =
        new JeroMQInstanceConnectionServiceModule()
            .withDefaultRefreshInterval()
            .withBindAddress(bindAddress);

    private NodeModuleFactory nodeModuleFactory = applicationId -> {
        throw new UnsupportedOperationException("Node.Factory not supported: ");
    };

    public JeroMQEmbeddedWorkerInstanceContainer() {
        withInstanceModules(
            new TestWorkerInstanceModule(),
            new TestMasterNodeModule(getInstanceId()),
            new SimpleExecutorsModule().withDefaultSchedulerThreads(),
            jeroMQInstanceConnectionServiceModule,
            new AbstractModule() {
                @Override
                protected void configure() {
                    final var parent = getProvider(Injector.class);
                    bind(new TypeLiteral<Set<Node>>(){}).toProvider(() -> loadNodes(parent.get()));
                    bind(Node.Factory.class).toInstance(aid -> {
                        final var nodeId = NodeId.forInstanceAndApplication(getInstanceId(), aid);
                        final var suppliedModules = nodeModuleFactory.create(nodeId);
                        final var module = new TestApplicationNodeModule(nodeId, suppliedModules);
                        return parent.get().createChildInjector(module).getInstance(Node.class);
                    });
                }
            }
        );
    }

    private Set<Node> loadNodes(final Injector parent) {

        if (!applicationIdInjectorMap.isEmpty()) {
            throw new IllegalStateException("Expected empty map.");
        }

        final var applicationIdInjectorMap = applicationModules
            .stream()
            .map(parent::createChildInjector)
            .collect(toMap(
                injector -> injector.getInstance(NodeId.class),
                i -> i,
                (l, r) -> l,
                LinkedHashMap::new
            )
        );

        this.applicationIdInjectorMap.putAll(applicationIdInjectorMap);

        return applicationIdInjectorMap
            .values()
            .stream()
            .map(injector -> injector.getInstance(Node.class))
            .collect(toSet());

    }

    @Override
    protected void doStart(ZContext zContext) {
        addConnectAddress(bindAddress);
        super.doStart(zContext);
    }

    @Override
    public JeroMQEmbeddedWorkerInstanceContainer clearConnectAddresses() {
        return (JeroMQEmbeddedWorkerInstanceContainer) super.clearConnectAddresses();
    }

    @Override
    public JeroMQEmbeddedWorkerInstanceContainer withInstanceId(InstanceId instanceId) {
        return (JeroMQEmbeddedWorkerInstanceContainer) super.withInstanceId(instanceId);
    }

    @Override
    public JeroMQEmbeddedWorkerInstanceContainer withInstanceModules(Module module) {
        return (JeroMQEmbeddedWorkerInstanceContainer) super.withInstanceModules(module);
    }

    @Override
    public JeroMQEmbeddedWorkerInstanceContainer withInstanceModules(Module module, Module... modules) {
        return (JeroMQEmbeddedWorkerInstanceContainer) super.withInstanceModules(module, modules);
    }

    @Override
    public JeroMQEmbeddedWorkerInstanceContainer withConnectAddress(String address) {
        return (JeroMQEmbeddedWorkerInstanceContainer) super.withConnectAddress(address);
    }

    @Override
    public JeroMQEmbeddedWorkerInstanceContainer withConnectAddress(String address, String... addresses) {
        return (JeroMQEmbeddedWorkerInstanceContainer) super.withConnectAddress(address, addresses);
    }

    @Override
    public JeroMQEmbeddedWorkerInstanceContainer withZContext(ZContext zContext) {
        return (JeroMQEmbeddedWorkerInstanceContainer) super.withZContext(zContext);
    }

    public JeroMQEmbeddedWorkerInstanceContainer withDefaultHttpClient() {
        return withInstanceModules(new JerseyHttpClientModule());
    }

    @Override
    public Worker getWorker() {
        return getInjector().getInstance(Worker.class);
    }

    @Override
    public String getBindAddress() {
        return bindAddress;
    }

    public JeroMQEmbeddedWorkerInstanceContainer withBindAddress(final String bindAddress) {
        checkNotRunning();
        requireNonNull(bindAddress, "bindAddress");
        jeroMQInstanceConnectionServiceModule.withBindAddress(this.bindAddress = bindAddress);
        return this;
    }

    public <ChainedT> ApplicationNodeBuilder<ChainedT> withApplication(final ApplicationId applicationId,
                                                                       final Supplier<ChainedT> chainedTSupplier) {
        return new ApplicationNodeBuilder<>(applicationId, chainedTSupplier);
    }

    public JeroMQEmbeddedWorkerInstanceContainer withNodeModuleFactory(final NodeModuleFactory nodeModuleFactory) {
        checkNotRunning();
        requireNonNull(nodeModuleFactory, "injectorNodeFactory");
        this.nodeModuleFactory = nodeModuleFactory;
        return this;
    }

    @Override
    protected void doClose() {
        applicationIdInjectorMap.clear();
        super.doClose();
    }

    public class ApplicationNodeBuilder<ChainedT> {

        private final NodeId nodeId;

        private final List<Module> nodeModules = new ArrayList<>();

        private final Supplier<ChainedT> chainedTSupplier;

        private ApplicationNodeBuilder(final ApplicationId applicationId, final Supplier<ChainedT> chainedTSupplier) {
            nodeId = NodeId.forInstanceAndApplication(getInstanceId(), applicationId);
            this.chainedTSupplier = chainedTSupplier;
        }

        public ApplicationNodeBuilder<ChainedT> withNodeModules(final Module module) {
            requireNonNull(module, "module");
            nodeModules.add(module);
            return this;
        }

        public ApplicationNodeBuilder<ChainedT> withNodeModules(final Module module, final Module ... additional) {
            requireNonNull(module, "module");
            requireNonNull(additional, "additional");
            nodeModules.add(module);
            nodeModules.addAll(asList(additional));
            return this;
        }

        public ChainedT endApplication() {
            checkNotRunning();
            final var module = new TestApplicationNodeModule(nodeId, nodeModules);
            applicationModules.add(module);
            return chainedTSupplier.get();
        }

    }

    @Override
    public IocResolver getIocResolver() {
        checkRunning();
        final var injector = applicationIdInjectorMap.values().iterator().next();
        return injector.getInstance(IocResolver.class);
    }

    @Override
    public IocResolver getIocResolver(final NodeId nodeId) {
        checkRunning();
        final var injector = applicationIdInjectorMap.get(nodeId);
        if (injector == null) throw new IllegalArgumentException("Unknown NodeId: " + nodeId);
        return injector.getInstance(IocResolver.class);
    }

    /**
     * Creates a new {@link Node}.
     */
    @FunctionalInterface
    public interface NodeModuleFactory {

        /**
         * Creates a new {@link Node} given the {@link Injector} and {@link ApplicationId}.
         *
         * @param nodeId the {@link NodeId} to use when creating the {@link Node}
         *
         * @return a {@link List<Module>} with all user-supplied {@link Module}s required to start the {@link Node}
         */
        Collection<? extends Module> create(NodeId nodeId);

    }

}
