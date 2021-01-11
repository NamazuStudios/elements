package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.Subscription;
import com.namazustudios.socialengine.rt.exception.InstanceNotFoundException;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.exception.NodeNotFoundException;
import com.namazustudios.socialengine.rt.id.ApplicationId;
import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.remote.InstanceConnectionService.InstanceConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.namazustudios.socialengine.rt.remote.RemoteInvokerRegistrySnapshot.*;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

public class SimpleRemoteInvokerRegistry implements RemoteInvokerRegistry {

    private static final Logger logger = LoggerFactory.getLogger(SimpleRemoteInvokerRegistry.class);

    private static final long REFRESH_RATE = 30;

    private static final TimeUnit REFRESH_UNITS = SECONDS;

    private static final long SHUTDOWN_TIMEOUT = 1;

    private static final TimeUnit SHUTDOWN_UNITS = MINUTES;

    private InstanceId instanceId;

    private Provider<RemoteInvoker> remoteInvokerProvider;

    private InstanceConnectionService instanceConnectionService;

    private final AtomicReference<RegistryContext> context = new AtomicReference<>();

    @Override
    public void start() {

        final RegistryContext context = new RegistryContext();

        if (this.context.compareAndSet(null, context)) {
            context.start();
        } else {
            throw new IllegalStateException("Already started.");
        }

    }

    @Override
    public void stop() {

        final RegistryContext context = this.context.getAndSet(null);

        if (context == null) {
            throw new IllegalStateException("Not running.");
        } else {
            context.stop();
        }

    }

    @Override
    public void refresh() {
        final RegistryContext context = getContext();
        context.refresh();
    }

    @Override
    public RemoteInvoker getBestRemoteInvoker(final ApplicationId applicationId) {
        final RemoteInvokerRegistrySnapshot snapshot = getSnapshot();
        return snapshot.getBestInvokerForApplication(applicationId);
    }

    @Override
    public List<RemoteInvoker> getAllRemoteInvokers(final ApplicationId applicationId) {
        final RemoteInvokerRegistrySnapshot snapshot = getSnapshot();
        return snapshot.getAllRemoteInvokersForApplication(applicationId);
    }

    @Override
    public RemoteInvoker getRemoteInvoker(final NodeId nodeId) {
        final RemoteInvokerRegistrySnapshot snapshot = getSnapshot();
        final RemoteInvoker remoteInvoker = snapshot.getRemoteInvoker(nodeId);
        if (remoteInvoker == null) throw new NodeNotFoundException("No RemoteInvoker for: " + nodeId);
        return remoteInvoker;
    }

    private RegistryContext getContext() {
        final RegistryContext context = this.context.get();
        if (context == null) throw new IllegalStateException("Not running.");
        return context;
    }

    private RemoteInvokerRegistrySnapshot getSnapshot() {
        final RegistryContext context = this.context.get();
        if (context == null) throw new IllegalStateException("Not running.");
        return context.snapshot;
    }

    public InstanceId getInstanceId() {
        return instanceId;
    }

    @Inject
    public void setInstanceId(InstanceId instanceId) {
        this.instanceId = instanceId;
    }

    public InstanceConnectionService getInstanceConnectionService() {
        return instanceConnectionService;
    }

    @Inject
    public void setInstanceConnectionService(InstanceConnectionService instanceConnectionService) {
        this.instanceConnectionService = instanceConnectionService;
    }

    public Provider<RemoteInvoker> getRemoteInvokerProvider() {
        return remoteInvokerProvider;
    }

    @Inject
    public void setRemoteInvokerProvider(Provider<RemoteInvoker> remoteInvokerProvider) {
        this.remoteInvokerProvider = remoteInvokerProvider;
    }

    private class RegistryContext {

        private Subscription connect;

        private Subscription disconnect;

        private ScheduledExecutorService scheduledExecutorService;

        private final CountDownLatch latch = new CountDownLatch(1);

        private final RemoteInvokerRegistrySnapshot snapshot = new RemoteInvokerRegistrySnapshot();

        private void start() {

            scheduledExecutorService = newSingleThreadScheduledExecutor(r -> {
                final Thread thread = new Thread(r);
                thread.setDaemon(true);
                thread.setName(SimpleRemoteInvokerRegistry.class.getSimpleName() + " refresher " + getInstanceId());
                thread.setUncaughtExceptionHandler(((t, e) -> logger.error("Caught exception in {}", t, e)));
                return thread;
            });

            scheduledExecutorService.scheduleAtFixedRate(this::doRefresh, 0, REFRESH_RATE, REFRESH_UNITS);
            connect = getInstanceConnectionService().subscribeToConnect(this::add);
            disconnect = getInstanceConnectionService().subscribeToDisconnect(this::remove);

            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new InternalException(e);
            }

        }

        private void stop() {

            connect.unsubscribe();
            disconnect.unsubscribe();
            scheduledExecutorService.shutdown();

            try {
                scheduledExecutorService.awaitTermination(SHUTDOWN_TIMEOUT, SHUTDOWN_UNITS);
            } catch (InterruptedException e) {
                throw new InternalException(e);
            }

            snapshot.clear();

        }

        private void add(final InstanceConnection connection) {
            final RefreshBuilder builder = snapshot.refresh();
            add(builder, connection).commit((ri, ex) -> {

                if (ex == null) {
                    logger.info("Cleaning up {}", ri);
                } else {
                    logger.error("Cleaning up {}", ri, ex);
                }

                if (ri != null) ri.stop();

            });
        }

        private void remove(final InstanceConnection connection) {

            final RefreshBuilder builder = snapshot.refresh();
            final InstanceId instanceId = connection.getInstanceId();

            builder.remove(instanceId).commit((ri, ex) -> {

                if (ex == null) {
                    logger.info("Cleaning up {}", ri);
                } else {
                    logger.error("Cleaning up {}", ri, ex);
                }

                if (ri != null) ri.stop();

            });

        }

        private void refresh() {

            final var latch = new CountDownLatch(1);
            scheduledExecutorService.submit(() -> doRefresh(latch));

            try {
                latch.await();
            } catch (InterruptedException ex) {
                throw new InternalException(ex);
            }

        }

        private void doRefresh() {
            doRefresh(latch);
        }

        private void doRefresh(final CountDownLatch latch) {
            try {

                final RefreshBuilder builder = snapshot.refresh();
                final List<InstanceConnection> connections = getInstanceConnectionService().getActiveConnections();

                for (final InstanceConnection connection : connections) {
                    add(builder, connection);
                }

                builder.prune().commit((ri, ex) -> {

                    if (ex == null) {
                        logger.info("Cleaning up {}", ri);
                    } else {
                        logger.error("Cleaning up {}", ri, ex);
                    }

                    if (ri != null) ri.stop();

                });

            } catch (Exception ex) {
                logger.error("Caught error refreshing instances.", ex);
            } finally {
                latch.countDown();
            }
        }

        private RefreshBuilder add(final RefreshBuilder builder, final InstanceConnection connection) {

            final double quality;
            final Set<NodeId> nodeIdSet;
            final InstanceId instanceId = connection.getInstanceId();

            try {
                logger.debug("Establishing quality for {}", connection.getInstanceId());
                quality = connection.getInstanceMetadataContext().getInstanceQuality();
                logger.debug("Established quality for {} as {}", connection.getInstanceId(), quality);
            } catch (InstanceNotFoundException | NodeNotFoundException ex) {
                logger.debug("Instance or node not found.", ex);
                return builder;
            } catch (Exception ex) {
                logger.error("Could not determine load average for instance {}", instanceId, ex);
                return builder;
            }

            try {
                logger.debug("Finding nodes for {}", connection.getInstanceId());
                nodeIdSet = connection.getInstanceMetadataContext().getNodeIds();
                logger.debug("Found nodes for {} as {}", connection.getInstanceId(), nodeIdSet);
            } catch (InstanceNotFoundException | NodeNotFoundException ex) {
                logger.debug("Instance or node not found.", ex);
                return builder;
            } catch (Exception ex) {
                logger.error("Could not node id set for instance {}", instanceId, ex);
                return builder;
            }

            for (final NodeId nodeId : nodeIdSet) {
                builder.add(nodeId, quality, () -> establishNewConnection(nodeId, connection));
            }

            return builder;

        }

        private RemoteInvoker establishNewConnection(final NodeId nodeId, final InstanceConnection connection) {
            final String addr = connection.openRouteToNode(nodeId);
            final RemoteInvoker remoteInvoker = getRemoteInvokerProvider().get();
            logger.info("Connecting to node {} via address {}", nodeId, addr);
            remoteInvoker.start(addr);
            return remoteInvoker;
        }

    }

}
