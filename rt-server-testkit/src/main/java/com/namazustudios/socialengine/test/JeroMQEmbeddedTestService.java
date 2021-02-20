package com.namazustudios.socialengine.test;

import com.google.inject.Module;
import com.namazustudios.socialengine.rt.IocResolver;
import com.namazustudios.socialengine.rt.Publisher;
import com.namazustudios.socialengine.rt.SimplePublisher;
import com.namazustudios.socialengine.rt.Subscription;
import com.namazustudios.socialengine.rt.exception.MultiException;
import com.namazustudios.socialengine.rt.id.ApplicationId;
import com.namazustudios.socialengine.rt.remote.Instance;
import com.namazustudios.socialengine.rt.remote.Worker;
import com.namazustudios.socialengine.test.JeroMQEmbeddedWorkerInstanceContainer.ApplicationNodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;

import javax.ws.rs.client.Client;
import java.util.ArrayList;
import java.util.List;
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

    public JeroMQEmbeddedTestService withClient() {
        if (client == null) client = new JeroMQEmbeddedClientInstanceContainer();
        return this;
    }

    public JeroMQEmbeddedTestService withWorker() {
        if (worker == null) worker = new JeroMQEmbeddedWorkerInstanceContainer();
        return this;
    }

    /**
     * Installs the supplied {@link Module} to the client {@link Instance}.
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
    public ApplicationNodeBuilder<JeroMQEmbeddedTestService> withApplication() {
        final var applicationId = ApplicationId.randomApplicationId();
        return withApplication(applicationId);
    }

    /**
     * Begins specialized configuration for an application with the specified unique name.
     *
     * @param uniqueName the application unique name {@see {@link ApplicationId#forUniqueName(String)}}
     *
     * @return a new instance of {@link ApplicationNodeBuilder}
     */
    public ApplicationNodeBuilder<JeroMQEmbeddedTestService> withApplication(final String uniqueName) {
        final var applicationId = ApplicationId.forUniqueName(uniqueName);
        return withApplication(applicationId);
    }

    /**
     * Begins specialized configuration for an application with the specified {@link ApplicationId}.
     *
     * @param applicationId the an {@link ApplicationId}
     *
     * @return a new instance of {@link ApplicationNodeBuilder}
     */
    public ApplicationNodeBuilder<JeroMQEmbeddedTestService> withApplication(final ApplicationId applicationId) {
        return withWorker().worker.withApplication(applicationId, () -> this);
    }

    @Override
    public EmbeddedTestService start() {

        if (worker == null && client == null) {
            throw new IllegalStateException("Must have at least one worker or client.");
        } else if (!running.compareAndSet(false, true)) {
            throw new IllegalStateException("Already started.");
        }

        final List<Exception> exceptionList = new ArrayList<>();

        try {
            if (getWorker() != null) getWorker().start();
        } catch (Exception ex) {
            exceptionList.add(ex);
            logger.error("Exception starting test worker instance.", ex);
        }

        try {
            if (getClient() != null) getClient().start();
        } catch (Exception ex) {
            exceptionList.add(ex);
            logger.error("Exception starting test client instance.", ex);
        }

        if (!exceptionList.isEmpty()) throw new MultiException(exceptionList);

        if (getWorker() != null) getWorker().getInstance().refreshConnections();
        if (getClient() != null) getClient().getInstance().refreshConnections();

        return this;

    }

    @Override
    public EmbeddedInstanceContainer getClient() {
        return client;
    }

    @Override
    public EmbeddedWorkerInstanceContainer getWorker() {
        return worker;
    }

    @Override
    public IocResolver getClientIocResolver() {
        return client.getIocResolver();
    }

    @Override
    public IocResolver getWorkerIocResolver() {
        return worker.getIocResolver();
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
