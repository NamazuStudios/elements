package dev.getelements.elements.test;

import com.google.inject.Module;
import dev.getelements.elements.rt.Publisher;
import dev.getelements.elements.rt.SimplePublisher;
import dev.getelements.elements.rt.Subscription;
import dev.getelements.elements.rt.exception.MultiException;
import dev.getelements.elements.rt.id.ApplicationId;
import dev.getelements.elements.rt.remote.Instance;
import dev.getelements.elements.rt.remote.Node;
import dev.getelements.elements.rt.remote.Worker;
import dev.getelements.elements.rt.transact.JournalTransactionalResourceServicePersistenceModule;
import dev.getelements.elements.rt.transact.unix.UnixFSTransactionalPersistenceContextModule;
import dev.getelements.elements.rt.xodus.XodusEnvironmentModule;
import dev.getelements.elements.rt.xodus.XodusTransactionalResourceServicePersistenceModule;
import dev.getelements.elements.test.JeroMQEmbeddedWorkerInstanceContainer.ApplicationNodeBuilder;
import dev.getelements.elements.test.JeroMQEmbeddedWorkerInstanceContainer.NodeModuleFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;

import javax.ws.rs.client.Client;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Embeds a test kit which supplies two {@link Instance}s. One which runs the client, and one which runs a worker.
 */
public class JeroMQEmbeddedTestService implements EmbeddedTestService {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQEmbeddedTestService.class);

    private ZContext zContext;

    private JeroMQEmbeddedClientInstanceContainer client;

    private JeroMQEmbeddedWorkerInstanceContainer worker;

    private final AtomicBoolean running = new AtomicBoolean();

    private final Publisher<JeroMQEmbeddedTestService> onClosePublisher = new SimplePublisher<>();

    /**
     * Configures a client {@link Instance}.
     *
     * @return this instance
     */
    public JeroMQEmbeddedTestService withClient() {
        if (client == null) client = new JeroMQEmbeddedClientInstanceContainer();
        return this;
    }

    /**
     * Configures a worker {@link Instance}.
     *
     * @return this instance
     */
    public JeroMQEmbeddedTestService withWorker() {
        if (worker == null) worker = new JeroMQEmbeddedWorkerInstanceContainer();
        return this;
    }

    public JeroMQEmbeddedTestService withZContext(final ZContext zContext) {
        this.zContext = zContext;
        return this;
    }

    /**
     * Installs the supplied {@link Module} to the worker {@link Instance}.
     *
     * If no worker instance was configured, this will implicitly configure a worker instance.
     *
     * @param module the module
     * @return this instance
     */
    public JeroMQEmbeddedTestService withWorkerModule(final Module module) {
        withWorker().worker.withInstanceModules(module);
        return this;
    }

    /**
     * Installs the supplied {@link Module} to the client {@link Instance}.
     *
     * If no client instance was configured, this will implicitly configure a client instance.
     *
     * @param module the module
     * @return this instance
     */
    public JeroMQEmbeddedTestService withClientModule(final Module module) {
        withClient().client.withInstanceModules(module);
        return this;
    }

    /**
     * Installs the default {@link Client} to the worker {@link Instance}.
     *
     * If no worker instance was configured, this will implicitly configure a worker instance.
     *
     * @return this instance
     */
    public JeroMQEmbeddedTestService withDefaultHttpClient() {
        withWorker().worker.withDefaultHttpClient();
        return this;
    }

    /**
     * Specifies the {@link Worker} bind address.
     *
     * @param workerBindAddress the bind address for the worker
     * @return this instance
     *
     */
    public JeroMQEmbeddedTestService withWorkerBindAddress(final String workerBindAddress) {
        withWorker().worker.withBindAddress(workerBindAddress);
        return this;
    }

    /**
     * Adds a randomly-assigned
     * @return
     */
    public ApplicationNodeBuilder<JeroMQEmbeddedTestService> withApplicationNode() {
        final var applicationId = ApplicationId.randomApplicationId();
        return withApplicationNode(applicationId);
    }

    /**
     * Begins specialized configuration for an application with the specified unique name.
     *
     * @param uniqueName the application unique name {@see {@link ApplicationId#forUniqueName(String)}}
     *
     * @return a new instance of {@link ApplicationNodeBuilder}
     */
    public ApplicationNodeBuilder<JeroMQEmbeddedTestService> withApplicationNode(final String uniqueName) {
        final var applicationId = ApplicationId.forUniqueName(uniqueName);
        return withApplicationNode(applicationId);
    }

    /**
     * Begins specialized configuration for an application with the specified {@link ApplicationId}.
     *
     * @param applicationId the an {@link ApplicationId}
     *
     * @return a new instance of {@link ApplicationNodeBuilder}
     */
    public ApplicationNodeBuilder<JeroMQEmbeddedTestService> withApplicationNode(final ApplicationId applicationId) {
        return withWorker().worker.withApplication(applicationId, () -> this);
    }

    /**
     * Specifies an {@link NodeModuleFactory} to use when creating new {@link Node}s in the worker instance.
     *
     * @param nodeModuleFactory the {@link NodeModuleFactory}
     *
     * @return the this instance
     */
    public JeroMQEmbeddedTestService withNodeModuleFactory(final NodeModuleFactory nodeModuleFactory) {
        withWorker().worker.withNodeModuleFactory(nodeModuleFactory);
        return this;
    }

    /**
     * Configures the {@link Worker} to use the UnixFS Storage system.
     *
     * @return this instance
     */
    public JeroMQEmbeddedTestService withUnixFSWorker() {

        withWorker().worker.withInstanceModules(
            new JournalTransactionalResourceServicePersistenceModule(),
            new UnixFSTransactionalPersistenceContextModule().withTestingDefaults(),
            new XodusEnvironmentModule()
                .withTempSchedulerEnvironment()
                .withTempResourceEnvironment()
        );

        return this;

    }

    /**
     * Configures the {@link Worker} to use the Xodus-based Storage system.
     *
     * @return this instance
     */
    public JeroMQEmbeddedTestService withXodusWorker() {

        withWorker().worker.withInstanceModules(
            new XodusEnvironmentModule()
                .withTempSchedulerEnvironment()
                .withTempResourceEnvironment(),
            new XodusTransactionalResourceServicePersistenceModule()
                .withDefaultBlockSize()
        );

        return this;

    }

    @Override
    public EmbeddedTestService start() {

        if (worker == null && client == null) {
            throw new IllegalStateException("Must have at least one worker or client.");
        } else if (!running.compareAndSet(false, true)) {
            throw new IllegalStateException("Already started.");
        }

        final ZContext zc;

        if (zContext == null) {
            final var created = zc = new ZContext();
            onClose(s -> created.close());
        } else {
            zc = zContext;
        }

        if (worker != null) worker.withZContext(zc);

        if (client != null) {
            client.withZContext(zc);
            client.clearConnectAddresses()
                  .withConnectAddress(worker.getBindAddress());
        }

        final List<Exception> exceptionList = new ArrayList<>();

        try {
            getWorkerOptional().ifPresent(EmbeddedWorkerInstanceContainer::start);
        } catch (Exception ex) {
            exceptionList.add(ex);
            logger.error("Exception starting test worker instance.", ex);
        }

        try {
            getClientOptional().ifPresent(EmbeddedClientInstanceContainer::start);
        } catch (Exception ex) {
            exceptionList.add(ex);
            logger.error("Exception starting test client instance.", ex);
        }

        if (!exceptionList.isEmpty()) throw new MultiException(exceptionList);

        getWorkerOptional().ifPresent(w -> w.getInstance().refreshConnections());
        getClientOptional().ifPresent(c -> c.getInstance().refreshConnections());

        return this;

    }

    @Override
    public Optional<EmbeddedClientInstanceContainer> getClientOptional() {
        return Optional.ofNullable(client);
    }

    @Override
    public Optional<EmbeddedWorkerInstanceContainer> getWorkerOptional() {
        return Optional.of(worker);
    }

    @Override
    public Subscription onClose(Consumer<? super EmbeddedTestService> consumer) {
        return onClosePublisher.subscribe(consumer);
    }

    @Override
    public void close() {

        final List<Exception> exceptionList = new ArrayList<>();

        try {
            if (client != null) getClient().close();
        } catch (Exception ex) {
            exceptionList.add(ex);
            logger.error("Exception stopping test client instance.", ex);
        }

        try {
            if (client != null) getWorker().close();
        } catch (Exception ex) {
            exceptionList.add(ex);
            logger.error("Exception stopping test worker instance.", ex);
        }

        if (!exceptionList.isEmpty()) throw new MultiException(exceptionList);

    }

}
