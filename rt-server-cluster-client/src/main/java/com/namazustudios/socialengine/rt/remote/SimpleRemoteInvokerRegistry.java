package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.InstanceMetadataContext;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.exception.NodeNotFoundException;
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

    private static final long REFRESH_RATE = 5;

    private static final TimeUnit REFRESH_UNITS = SECONDS;

    private static final long SHUTDOWN_TIMEOUT = 1;

    private static final TimeUnit SHUTDOWN_UNITS = MINUTES;

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
    public RemoteInvoker getAnyRemoteInvoker(final UUID applicationId) {
        final RemoteInvokerRegistrySnapshot snapshot = getSnapshot();
        return snapshot.getBestInvokerForApplication(applicationId);
    }

    @Override
    public List<RemoteInvoker> getAllRemoteInvokers(UUID applicationId) {
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

    private RemoteInvokerRegistrySnapshot getSnapshot() {
        final RegistryContext context = this.context.get();
        if (context == null) throw new IllegalStateException("Not running.");
        return context.snapshot;
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

        private InstanceConnectionService.Subscription connect;

        private InstanceConnectionService.Subscription disconnect;

        private ScheduledExecutorService scheduledExecutorService;

        private final CountDownLatch latch = new CountDownLatch(1);

        private final RemoteInvokerRegistrySnapshot snapshot = new RemoteInvokerRegistrySnapshot();

        private void start() {

            scheduledExecutorService = newSingleThreadScheduledExecutor();
            scheduledExecutorService.scheduleAtFixedRate(this::refresh, 0, REFRESH_RATE, REFRESH_UNITS);
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

        }

        private void add(final InstanceConnection connection) {
            final RefreshBuilder builder = snapshot.refresh();
            final InstanceMetadataContext context = connection.getInstanceMetadataContext();
            add(builder, context).commit((ri, ex) -> {
                if (ri != null) {
                    logger.error("Cleaning up RemoteInvoker {}", ri, ex);
                    ri.stop();
                }
            });
        }

        private void remove(final InstanceConnection connection) {

            final RefreshBuilder builder = snapshot.refresh();
            final InstanceId instanceId = connection.getInstanceId();

            builder.remove(instanceId).commit((ri, ex) -> {
                if (ri != null) {
                    logger.error("Cleaning up RemoteInvoker {}", ri, ex);
                    ri.stop();
                }
            });

        }

        private void refresh() {

            final RefreshBuilder builder = snapshot.refresh();
            final List<InstanceConnection> connections = getInstanceConnectionService().getActiveConnections();

            for (final InstanceConnection connection : connections) {
                final InstanceMetadataContext context = connection.getInstanceMetadataContext();
                add(builder, context);
            }

            builder.prune().commit((ri, ex) -> {
                if (ri != null) {
                    logger.error("Cleaning up RemoteInvoker {}", ri, ex);
                    ri.stop();
                }
            });

            latch.countDown();

        }

        private RefreshBuilder add(final RefreshBuilder builder, final InstanceMetadataContext context) {

            final double load;
            final Set<NodeId> nodeIdSet;
            final InstanceId instanceId = context.getInstanceId();

            try {
                load = context.getLoadAverage();
            } catch (Exception ex) {
                logger.error("Could not determine load average for instance {}", instanceId, ex);
                return builder;
            }

            try {
                nodeIdSet = context.getNodeIds();
            } catch (Exception ex) {
                logger.error("Could not node id set for instance {}", instanceId, ex);
                return builder;
            }

            for (final NodeId nodeId : nodeIdSet) {
                builder.add(nodeId, load, () -> establishNewConnection(nodeId));
            }

            return builder;

        }

        private RemoteInvoker establishNewConnection(NodeId nodeId) {
            final String addr = getInstanceConnectionService().getRoute(nodeId);
            final RemoteInvoker remoteInvoker = getRemoteInvokerProvider().get();
            remoteInvoker.start(addr);
            return remoteInvoker;
        }

    }

}
