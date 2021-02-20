package com.namazustudios.socialengine.test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.namazustudios.socialengine.rt.IocResolver;
import com.namazustudios.socialengine.rt.Publisher;
import com.namazustudios.socialengine.rt.SimplePublisher;
import com.namazustudios.socialengine.rt.Subscription;
import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.remote.Instance;
import com.namazustudios.socialengine.rt.remote.RemoteInvokerRegistry;
import com.namazustudios.socialengine.rt.remote.SimpleRemoteInvokerRegistry;
import com.namazustudios.socialengine.rt.remote.guice.StaticInstanceDiscoveryServiceModule;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.JeroMQAsyncConnectionServiceModule;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.JeroMQControlClientModule;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.JeroMQRemoteInvokerModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static com.namazustudios.socialengine.rt.id.InstanceId.randomInstanceId;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static org.zeromq.ZContext.shadow;

public class JeroMQEmbeddedInstanceContainer implements EmbeddedInstanceContainer {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQEmbeddedInstanceContainer.class);

    public static final int MINIMUM_CONNECTIONS = 5;

    public static final int MAXIMUM_CONNECTIONS = 250;

    // Transient Fields

    private Instance instance;

    private Injector injector;

    private final AtomicBoolean running = new AtomicBoolean();

    // Configuration Fields (non-final)

    private ZContext zContext;

    private InstanceId instanceId = randomInstanceId();

    // Configuration Fields (final)

    private final List<Module> instanceModules = new ArrayList<>();

    private final List<String> connectAddresses = new ArrayList<>();

    private final Publisher<EmbeddedInstanceContainer> onClosePublisher = new SimplePublisher<>();

    /**
     * Specifies the instance id, if not specified then this returns null.
     * @param instanceId the instanceid
     * @return this instance
     */
    public JeroMQEmbeddedInstanceContainer withInstanceId(final InstanceId instanceId) {
        checkNotRunning();
        requireNonNull(instanceId, "instanceId");
        this.instanceId = instanceId;
        return this;
    }

    public JeroMQEmbeddedInstanceContainer withInstanceModules(final Module module) {
        checkNotRunning();
        requireNonNull(module, "module");
        instanceModules.add(module);
        return this;
    }

    public JeroMQEmbeddedInstanceContainer withInstanceModules(final Module module, final Module ... modules) {
        checkNotRunning();
        requireNonNull(module, "module");
        requireNonNull(modules, "modules");
        instanceModules.add(module);
        instanceModules.addAll(asList(modules));
        return this;
    }

    public JeroMQEmbeddedInstanceContainer withConnectAddress(final String address) {
        checkNotRunning();
        requireNonNull(address, "address");
        connectAddresses.add(address);
        return this;
    }

    public JeroMQEmbeddedInstanceContainer withConnectAddress(final String address,
                                                              final String ... addresses) {
        checkNotRunning();
        requireNonNull(address, "address");
        requireNonNull(addresses, "addresses");
        connectAddresses.add(address);
        connectAddresses.addAll(asList(addresses));
        return this;
    }

    public JeroMQEmbeddedInstanceContainer withZContext(final ZContext zContext) {
        checkNotRunning();
        requireNonNull(zContext);
        this.zContext = zContext;
        return this;
    }

    protected void checkRunning() {
        if (!running.get()) throw new IllegalStateException("Already running.");
    }

    protected void checkNotRunning() {
        if (running.get()) throw new IllegalStateException("Already running.");
    }

    @Override
    public JeroMQEmbeddedInstanceContainer start() {

        if (zContext == null) {
            // This instance owns this ZContext, so it has to clean it up when it's finished with it.
            zContext = new ZContext();
            onClose(ic -> zContext.close());
        }

        if (!running.compareAndSet(false, true)) {
            throw new IllegalStateException("Already Running.");
        }

        doStart();
        return this;

    }

    protected void doStart() {

        final var zContext = shadow(this.zContext);

        final var module = new AbstractModule() {
            @Override
            protected void configure() {

                bind(ZContext.class).toInstance(zContext);
                bind(InstanceId.class).toInstance(instanceId);

                bind(RemoteInvokerRegistry.class)
                    .to(SimpleRemoteInvokerRegistry.class)
                    .asEagerSingleton();

                install(new StaticInstanceDiscoveryServiceModule()
                    .withInstanceAddresses(connectAddresses));

                install(new JeroMQRemoteInvokerModule()
                    .withMinimumConnections(MINIMUM_CONNECTIONS)
                    .withMaximumConnections(MAXIMUM_CONNECTIONS));

                install(new JeroMQControlClientModule());
                install(new JeroMQAsyncConnectionServiceModule());

                instanceModules.forEach(this::install);

            }
        };

        // Creates injector and starts the instance.
        injector = Guice.createInjector(module);
        instance = injector.getInstance(Instance.class);
        instance.start();

    }

    @Override
    public Subscription onClose(Consumer<? super EmbeddedInstanceContainer> consumer) {
        return onClosePublisher.subscribe(consumer);
    }

    @Override
    public void close() {
        if (running.compareAndSet(true, false)) doClose();
    }

    protected void doClose() {

        final var instance = injector.getInstance(Instance.class);

        try {
            instance.close();
        } catch (Exception ex) {
            logger.error("Error shutting down instance.", ex);
        }

        final var zContext = injector.getInstance(ZContext.class);

        try {
            zContext.close();
        } catch (Exception ex) {
            logger.error("Error shutting down ZContext.", ex);
        }

        onClosePublisher.publish(this);

    }

    @Override
    public Instance getInstance() {
        checkRunning();
        return instance;
    }

    @Override
    public InstanceId getInstanceId() {
        return instanceId;
    }

    @Override
    public IocResolver getIocResolver() {
        checkRunning();
        return injector.getInstance(IocResolver.class);
    }

}
