package dev.getelements.elements.rt.remote;

import dev.getelements.elements.rt.AsyncConnectionService;
import dev.getelements.elements.rt.exception.MultiException;
import dev.getelements.elements.sdk.cluster.id.InstanceId;
import dev.getelements.elements.sdk.util.ShutdownHooks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class SimpleInstance implements Instance {

    private static final ShutdownHooks hooks = new ShutdownHooks(SimpleInstance.class);

    private static final Logger logger = LoggerFactory.getLogger(SimpleInstance.class);

    protected InstanceId instanceId;

    private RemoteInvokerRegistry remoteInvokerRegistry;

    private InstanceDiscoveryService instanceDiscoveryService;

    private InstanceConnectionService instanceConnectionService;

    private AsyncConnectionService<?, ?> asyncConnectionService;

    private final AtomicBoolean closed = new AtomicBoolean();

    @Override
    public void start() {

        if (closed.get())
            throw new IllegalStateException("Instance is closed.");

        final List<Exception> exceptionList = new ArrayList<>();
        hooks.add(this::close);

        preStart(exceptionList::add);

        try {
            logger.debug("Starting async connection service. Instance ID {}", instanceId);
            getAsyncConnectionService().start();
            logger.debug("Started async connection service. Instance ID {}", instanceId);
        } catch (Exception ex) {
            exceptionList.add(ex);
            logger.error("Caught exception starting AsyncConnectionService.", ex);
        }

        try {
            logger.debug("Starting instance discovery service. Instance ID {}", instanceId);
            getInstanceDiscoveryService().start();
            logger.debug("Started instance discovery service. Instance ID {}", instanceId);
        } catch (Exception ex) {
            exceptionList.add(ex);
            logger.error("Caught exception starting InstanceDiscoveryService.", ex);
        }

        try {
            logger.debug("Starting instance connection service. Instance ID {}", instanceId);
            getInstanceConnectionService().start();
            logger.debug("Started instance connection service. Instance ID {}", instanceId);
        } catch (Exception ex) {
            exceptionList.add(ex);
            logger.error("Caught exception starting InstanceConnectionService.", ex);
        }

        try {
            logger.debug("Starting remote invoker registry. Instance ID {}", instanceId);
            getRemoteInvokerRegistry().start();
            logger.debug("Started remote invoker registry. Instance ID {}", instanceId);
        } catch (Exception ex) {
            exceptionList.add(ex);
            logger.error("Caught exception starting RemoteInvokerRegistry.", ex);
        }

        logger.debug("Running post-start tasks for Instance ID {}", instanceId);
        postStart(exceptionList::add);

        logger.debug("Completed post-start tasks for Instance ID {}", instanceId);

        if (!exceptionList.isEmpty()) {
            logger.error("One or more nodes failed to start up.");
            throw new MultiException(exceptionList);
        }

    }

    protected void preStart(final Consumer<Exception> exceptionConsumer) {}

    protected void postStart(final Consumer<Exception> exceptionConsumer) {}

    @Override
    public void close() {

        if (!closed.compareAndSet(false, true))
            return;

        final List<Exception> exceptionList = new ArrayList<>();

        preClose(exceptionList::add);

        try {
            logger.debug("Stopping remote invoker registry. Instance ID {}", instanceId);
            getRemoteInvokerRegistry().stop();
            logger.debug("Stopped remote invoker registry. Instance ID {}", instanceId);
        } catch (Exception ex) {
            exceptionList.add(ex);
            logger.error("Caught exception stopping RemoteInvokerRegistry.", ex);
        }

        try {
            logger.debug("Stopping instance connection service. Instance ID {}", instanceId);
            getInstanceConnectionService().stop();
            logger.debug("Stopped instance connection service. Instance ID {}", instanceId);
        } catch (Exception ex) {
            exceptionList.add(ex);
            logger.error("Caught exception stopping InstanceDiscoveryService.", ex);
        }

        try {
            getInstanceDiscoveryService().stop();
        } catch (Exception ex) {
            exceptionList.add(ex);
            logger.error("Caught exception stopping InstanceDiscoveryService.", ex);
        }

        try {
            getAsyncConnectionService().stop();
        } catch (Exception ex) {
            exceptionList.add(ex);
            logger.error("Caught exception stopping AsyncConnectionService.", ex);
        }

        postClose(exceptionList::add);

        if (!exceptionList.isEmpty())
            throw new MultiException(exceptionList);

    }

    protected void preClose(final Consumer<Exception> exceptionConsumer) {}

    protected void postClose(final Consumer<Exception> exceptionConsumer) {}

    @Override
    public void refreshConnections() {
        getInstanceConnectionService().refresh();
        getRemoteInvokerRegistry().refresh();
    }

    @Override
    public InstanceId getInstanceId() {
        return instanceId;
    }

    @Inject
    public void setInstanceId(InstanceId instanceId) {
        this.instanceId = instanceId;
    }

    public RemoteInvokerRegistry getRemoteInvokerRegistry() {
        return remoteInvokerRegistry;
    }

    @Inject
    public void setRemoteInvokerRegistry(RemoteInvokerRegistry remoteInvokerRegistry) {
        this.remoteInvokerRegistry = remoteInvokerRegistry;
    }

    public InstanceDiscoveryService getInstanceDiscoveryService() {
        return instanceDiscoveryService;
    }

    @Inject
    public void setInstanceDiscoveryService(InstanceDiscoveryService instanceDiscoveryService) {
        this.instanceDiscoveryService = instanceDiscoveryService;
    }

    public InstanceConnectionService getInstanceConnectionService() {
        return instanceConnectionService;
    }

    @Inject
    public void setInstanceConnectionService(InstanceConnectionService instanceConnectionService) {
        this.instanceConnectionService = instanceConnectionService;
    }

    public AsyncConnectionService<?, ?> getAsyncConnectionService() {
        return asyncConnectionService;
    }

    @Inject
    public void setAsyncConnectionService(AsyncConnectionService<?, ?> asyncConnectionService) {
        this.asyncConnectionService = asyncConnectionService;
    }

}
