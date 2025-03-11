package dev.getelements.elements.test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import dev.getelements.elements.sdk.util.Publisher;
import dev.getelements.elements.sdk.util.LinkedPublisher;
import dev.getelements.elements.sdk.Subscription;
import dev.getelements.elements.rt.kryo.guice.KryoPayloadReaderWriterModule;
import dev.getelements.elements.sdk.cluster.id.InstanceId;
import dev.getelements.elements.rt.remote.Instance;
import dev.getelements.elements.rt.remote.RemoteInvokerRegistry;
import dev.getelements.elements.rt.remote.SimpleRemoteInvokerRegistry;
import dev.getelements.elements.rt.remote.guice.StaticInstanceDiscoveryServiceModule;
import dev.getelements.elements.rt.remote.jeromq.JeroMQSecurity;
import dev.getelements.elements.rt.remote.jeromq.guice.JeroMQAsyncConnectionServiceModule;
import dev.getelements.elements.rt.remote.jeromq.guice.JeroMQControlClientModule;
import dev.getelements.elements.rt.remote.jeromq.guice.JeroMQRemoteInvokerModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.sdk.cluster.id.InstanceId.randomInstanceId;
import static dev.getelements.elements.rt.remote.SimpleRemoteInvokerRegistry.*;
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

    private JeroMQSecurity jeroMQSecurity;

    private InstanceId instanceId = randomInstanceId();

    // Configuration Fields (final)

    private final List<Module> instanceModules = new ArrayList<>();

    private final List<String> connectAddresses = new ArrayList<>();

    private final Publisher<EmbeddedInstanceContainer> onClosePublisher = new LinkedPublisher<>();

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

    public JeroMQEmbeddedInstanceContainer clearConnectAddresses() {
        checkNotRunning();
        connectAddresses.clear();
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

    public JeroMQEmbeddedInstanceContainer withSecurity(final JeroMQSecurity jeroMQSecurity) {
        checkNotRunning();
        requireNonNull(jeroMQSecurity);
        this.jeroMQSecurity = jeroMQSecurity;
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

        if (!running.compareAndSet(false, true)) {
            throw new IllegalStateException("Already Running.");
        }

        if (zContext == null) {
            final var created = new ZContext();
            onClose(s -> created.close());
            doStart(created);
        } else {
            doStart(zContext);
        }

        return this;

    }

    protected void doStart(final ZContext zContext) {

        final var zContextShadow = shadow(zContext);
        final var module = new TestInstanceModule(zContextShadow, jeroMQSecurity);

        // Creates injector and starts the instance.
        injector = Guice.createInjector(module);
        instance = injector.getInstance(Instance.class);
        instance.start();

    }

    protected void addConnectAddress(final String connectAddress) {
        if (!connectAddresses.contains(connectAddress)) {
            connectAddresses.add(connectAddress);
        }
    }

    protected Injector getInjector() {
        checkRunning();
        return injector;
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

    private class TestInstanceModule extends AbstractModule {

        private final ZContext zContext;

        private final JeroMQSecurity jeroMQSecurity;

        private TestInstanceModule(final ZContext zContext, final JeroMQSecurity jeroMQSecurity) {
            this.zContext = zContext;
            this.jeroMQSecurity = jeroMQSecurity == null ? JeroMQSecurity.DEFAULT : jeroMQSecurity;
        }

        @Override
        protected void configure() {

            bind(ZContext.class).toInstance(zContext);
            bind(InstanceId.class).toInstance(instanceId);
            bind(JeroMQSecurity.class).toInstance(jeroMQSecurity);

            bind(RemoteInvokerRegistry.class)
                .to(SimpleRemoteInvokerRegistry.class)
                .asEagerSingleton();

            install(new StaticInstanceDiscoveryServiceModule()
                .withInstanceAddresses(connectAddresses));

            install(new JeroMQRemoteInvokerModule()
                .withMinimumConnections(MINIMUM_CONNECTIONS)
                .withMaximumConnections(MAXIMUM_CONNECTIONS));

            install(new JeroMQControlClientModule());
            install(new KryoPayloadReaderWriterModule());
            install(new JeroMQAsyncConnectionServiceModule().withDefaultIoThreads());

            instanceModules.forEach(this::install);

            bind(Long.class)
                .annotatedWith(named(REFRESH_RATE_SECONDS))
                .toInstance(10L);

            bind(Long.class)
                .annotatedWith(named(REFRESH_TIMEOUT_SECONDS))
                .toInstance(15L);

            bind(Long.class)
                .annotatedWith(named(TOTAL_REFRESH_TIMEOUT_SECONDS))
                .toInstance(20L);

        }
    }

}
