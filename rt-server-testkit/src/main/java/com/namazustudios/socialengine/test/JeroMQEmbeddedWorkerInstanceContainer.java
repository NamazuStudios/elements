package com.namazustudios.socialengine.test;

import com.google.inject.Module;
import com.google.inject.*;
import com.namazustudios.socialengine.rt.IocResolver;
import com.namazustudios.socialengine.rt.fst.FSTPayloadReaderWriterModule;
import com.namazustudios.socialengine.rt.guice.SimpleExecutorsModule;
import com.namazustudios.socialengine.rt.id.ApplicationId;
import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.remote.Node;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.JeroMQInstanceConnectionServiceModule;
import com.namazustudios.socialengine.rt.transact.SimpleTransactionalResourceServicePersistenceModule;
import com.namazustudios.socialengine.rt.transact.unix.UnixFSTransactionalPersistenceContextModule;
import com.namazustudios.socialengine.test.guice.TestMasterNodeModule;
import com.namazustudios.socialengine.test.guice.TestWorkerInstanceModule;
import com.namazustudios.socialengine.test.guice.TestWorkerNodeModule;
import org.zeromq.ZContext;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

public class JeroMQEmbeddedWorkerInstanceContainer extends JeroMQEmbeddedInstanceContainer
                                                   implements EmbeddedWorkerInstanceContainer {

    private String bindAddress = format("inproc://%s.worker", randomUUID());

    private final List<Module> applicationModules = new ArrayList<>();

    private final Map<NodeId, Injector> applicationIdInjectorMap = new LinkedHashMap<>();

    public JeroMQEmbeddedWorkerInstanceContainer() {
        withInstanceModules(
            new TestWorkerInstanceModule(),
            new TestMasterNodeModule(getInstanceId()),
            new FSTPayloadReaderWriterModule(),
            new SimpleExecutorsModule().withDefaultSchedulerThreads(),
            new SimpleTransactionalResourceServicePersistenceModule(),
            new UnixFSTransactionalPersistenceContextModule().withTestingDefaults(),
            new JeroMQInstanceConnectionServiceModule()
                .withBindAddress(bindAddress)
                .withDefaultRefreshInterval(),
            new AbstractModule() {
                @Override
                protected void configure() {
                    bind(new TypeLiteral<Set<Node>>(){}).toProvider(JeroMQEmbeddedWorkerInstanceContainer.this::loadNodes);
                }
            }
        );

    }

    private Set<Node> loadNodes() {

        if (!applicationIdInjectorMap.isEmpty()) {
            throw new IllegalStateException("Expected empty map.");
        }

        final var applicationIdInjectorMap = applicationModules
            .stream()
            .map(Guice::createInjector)
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
        return withInstanceModules(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Client.class).toProvider(ClientBuilder::newClient).asEagerSingleton();
            }
        });
    }

    public JeroMQEmbeddedWorkerInstanceContainer withBindAddress(final String bindAddress) {
        requireNonNull(bindAddress, "bindAddress");
        this.bindAddress = bindAddress;
        return this;
    }

    public ApplicationNodeBuilder<JeroMQEmbeddedWorkerInstanceContainer> withApplication(
            final ApplicationId applicationId) {
        return new ApplicationNodeBuilder<>(applicationId, () -> this);
    }

    public <ChainedT> ApplicationNodeBuilder<ChainedT> withApplication(final ApplicationId applicationId,
                                                                       final Supplier<ChainedT> chainedTSupplier) {
        return new ApplicationNodeBuilder<>(applicationId, chainedTSupplier);
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

        public ApplicationNodeBuilder withNodeModules(final Module module) {
            requireNonNull(module, "module");
            nodeModules.add(module);
            return this;
        }

        public ApplicationNodeBuilder withNodeModules(final Module module, final Module ... additional) {
            requireNonNull(module, "module");
            requireNonNull(additional, "additional");
            nodeModules.add(module);
            nodeModules.addAll(asList(additional));
            return this;
        }

        public ChainedT build() {
            final var module = new TestWorkerNodeModule(nodeId, nodeModules);
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

}
