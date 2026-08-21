package dev.getelements.elements.cluster.fabric;

import dev.getelements.elements.rt.InstanceMetadataContext;
import dev.getelements.elements.rt.remote.*;
import dev.getelements.elements.sdk.Subscription;
import dev.getelements.elements.sdk.cluster.id.InstanceId;
import dev.getelements.elements.sdk.cluster.id.NodeId;
import dev.getelements.elements.sdk.util.AsyncPublisher;
import dev.getelements.elements.sdk.util.ConcurrentLockedPublisher;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * A {@link InstanceConnectionService} backed by the Fabric WebSocket transport (issue #10). Unlike the JeroMQ
 * implementation it replaces, this keeps no persistent connection to remote instances at all &mdash; each
 * discovered {@link InstanceHostInfo} is resolved into an {@link InstanceConnection} by minting a
 * {@link RemoteInvoker}, dialing it once to learn the remote's {@link InstanceId}, and otherwise treating every
 * subsequent call (including {@link InstanceMetadataContext} refreshes) as its own one-shot dial/ask/close.
 */
public class JakartaWebsocketInstanceConnectionService implements InstanceConnectionService {

    private static final Logger logger = LoggerFactory.getLogger(JakartaWebsocketInstanceConnectionService.class);

    private InstanceId instanceId;

    private InstanceDiscoveryService instanceDiscoveryService;

    private Provider<RemoteInvoker> remoteInvokerProvider;

    private final AtomicReference<Context> context = new AtomicReference<>();

    @Override
    public void start() {
        final var context = new Context();
        if (this.context.compareAndSet(null, context)) {
            context.start();
        } else {
            throw new IllegalStateException("Already started.");
        }
    }

    @Override
    public void stop() {
        final var context = this.context.getAndSet(null);
        if (context == null) {
            throw new IllegalStateException("Not running.");
        } else {
            context.stop();
        }
    }

    @Override
    public void refresh() {
        getContext().refresh();
    }

    @Override
    public InstanceId getInstanceId() {
        return instanceId;
    }

    @Inject
    public void setInstanceId(final InstanceId instanceId) {
        this.instanceId = instanceId;
    }

    public InstanceDiscoveryService getInstanceDiscoveryService() {
        return instanceDiscoveryService;
    }

    @Inject
    public void setInstanceDiscoveryService(final InstanceDiscoveryService instanceDiscoveryService) {
        this.instanceDiscoveryService = instanceDiscoveryService;
    }

    public Provider<RemoteInvoker> getRemoteInvokerProvider() {
        return remoteInvokerProvider;
    }

    @Inject
    public void setRemoteInvokerProvider(final Provider<RemoteInvoker> remoteInvokerProvider) {
        this.remoteInvokerProvider = remoteInvokerProvider;
    }

    @Override
    public InstanceBinding openBinding(final NodeId nodeId) {
        // No per-node binding concept exists in this transport - there is one static endpoint (/cluster/v1) per
        // instance, not a bind address per NodeId. This is a minimal placeholder pending real per-NodeId addressing
        // (deferred, see the Second Stage plan's Context section).
        return new InstanceBinding() {

            @Override
            public NodeId getNodeId() {
                return nodeId;
            }

            @Override
            public String getBindAddress() {
                return FabricEndpoint.CLUSTER_WS_PATH;
            }

            @Override
            public void close() {}

        };
    }

    @Override
    public List<InstanceConnection> getActiveConnections() {
        return getContext().getActive();
    }

    @Override
    public Subscription subscribeToConnect(final Consumer<InstanceConnection> onConnect) {
        return getContext().subscribeToConnect(onConnect);
    }

    @Override
    public Subscription subscribeToDisconnect(final Consumer<InstanceConnection> onDisconnect) {
        return getContext().subscribeToDisconnect(onDisconnect);
    }

    @Override
    public String getLocalControlAddress() {
        // No separate control-plane exists in this transport - every call, including metadata refresh, is a
        // one-shot Invocation against the same /cluster/v1 endpoint. Nothing depends on this today.
        return null;
    }

    private Context getContext() {
        final var context = this.context.get();
        if (context == null) throw new IllegalStateException("Not running.");
        return context;
    }

    private class Context {

        private final Lock lock = new ReentrantLock();

        private final Map<InstanceHostInfo, InstanceConnectionImpl> active = new HashMap<>();

        private final AsyncPublisher<InstanceConnection> onConnect = new ConcurrentLockedPublisher<>(lock);

        private final AsyncPublisher<InstanceConnection> onDisconnect = new ConcurrentLockedPublisher<>(lock);

        private Subscription onDiscover;

        private Subscription onUndiscover;

        private void start() {
            onDiscover = getInstanceDiscoveryService().subscribeToDiscovery(this::add);
            onUndiscover = getInstanceDiscoveryService().subscribeToUndiscovery(this::remove);
            refresh();
        }

        private void stop() {

            onDiscover.unsubscribe();
            onUndiscover.unsubscribe();

            final List<InstanceConnectionImpl> toRemove;

            lock.lock();

            try {
                toRemove = new ArrayList<>(active.values());
                active.clear();
            } finally {
                lock.unlock();
            }

            toRemove.forEach(c -> {
                onDisconnect.publishAsync(c);
                c.getRemoteInvoker().stop();
            });

        }

        private void refresh() {

            final var known = getInstanceDiscoveryService().getKnownHosts();

            final Set<InstanceHostInfo> toAdd;
            final Set<InstanceHostInfo> toRemove;

            lock.lock();

            try {
                toAdd = new HashSet<>(known);
                toAdd.removeAll(active.keySet());
                toRemove = new HashSet<>(active.keySet());
                toRemove.removeAll(known);
            } finally {
                lock.unlock();
            }

            toAdd.forEach(this::add);
            toRemove.forEach(this::remove);

        }

        private void add(final InstanceHostInfo instanceHostInfo) {

            lock.lock();

            try {
                if (active.containsKey(instanceHostInfo)) return;
            } finally {
                lock.unlock();
            }

            try {

                final var remoteInvoker = getRemoteInvokerProvider().get();
                remoteInvoker.start(instanceHostInfo.getConnectAddress() + FabricEndpoint.CLUSTER_WS_PATH);

                final var instanceMetadataContext = new ProxyBuilder<>(InstanceMetadataContext.class)
                        .dontProxyDefaultMethods()
                        .withDefaultHashCodeAndEquals()
                        .withHandlersForRemoteInvoker(remoteInvoker)
                        .build();

                final var remoteInstanceId = instanceMetadataContext.getInstanceId();

                final var connection = new InstanceConnectionImpl(
                        remoteInstanceId,
                        remoteInvoker,
                        instanceMetadataContext,
                        this::disconnect
                );

                final boolean added;

                lock.lock();

                try {
                    added = active.putIfAbsent(instanceHostInfo, connection) == null;
                } finally {
                    lock.unlock();
                }

                if (added) {
                    logger.info("Connected to instance {} at {}", remoteInstanceId, instanceHostInfo);
                    onConnect.publishAsync(connection);
                } else {
                    remoteInvoker.stop();
                }

            } catch (final Exception ex) {
                logger.warn("Failed to establish connection to {}", instanceHostInfo, ex);
            }

        }

        private void remove(final InstanceHostInfo instanceHostInfo) {

            final InstanceConnectionImpl removed;

            lock.lock();

            try {
                removed = active.remove(instanceHostInfo);
            } finally {
                lock.unlock();
            }

            if (removed != null) {
                logger.info("Disconnected from instance {} at {}", removed.getInstanceId(), instanceHostInfo);
                onDisconnect.publishAsync(removed);
                removed.getRemoteInvoker().stop();
            }

        }

        private void disconnect(final InstanceConnectionImpl connection) {

            final boolean removed;

            lock.lock();

            try {
                removed = active.values().remove(connection);
            } finally {
                lock.unlock();
            }

            if (removed) {
                onDisconnect.publishAsync(connection);
                connection.getRemoteInvoker().stop();
            }

        }

        private List<InstanceConnection> getActive() {
            lock.lock();
            try {
                return new ArrayList<>(active.values());
            } finally {
                lock.unlock();
            }
        }

        private Subscription subscribeToConnect(final Consumer<InstanceConnection> onConnect) {
            return this.onConnect.subscribe(onConnect);
        }

        private Subscription subscribeToDisconnect(final Consumer<InstanceConnection> onDisconnect) {
            return this.onDisconnect.subscribe(onDisconnect);
        }

    }

    private static class InstanceConnectionImpl implements InstanceConnection {

        private final InstanceId instanceId;

        private final RemoteInvoker remoteInvoker;

        private final InstanceMetadataContext instanceMetadataContext;

        private final Consumer<InstanceConnectionImpl> onDisconnect;

        private InstanceConnectionImpl(final InstanceId instanceId,
                                       final RemoteInvoker remoteInvoker,
                                       final InstanceMetadataContext instanceMetadataContext,
                                       final Consumer<InstanceConnectionImpl> onDisconnect) {
            this.instanceId = instanceId;
            this.remoteInvoker = remoteInvoker;
            this.instanceMetadataContext = instanceMetadataContext;
            this.onDisconnect = onDisconnect;
        }

        @Override
        public InstanceId getInstanceId() {
            return instanceId;
        }

        @Override
        public InstanceMetadataContext getInstanceMetadataContext() {
            return instanceMetadataContext;
        }

        @Override
        public RemoteInvoker getRemoteInvoker() {
            return remoteInvoker;
        }

        @Override
        public void disconnect() {
            onDisconnect.accept(this);
        }

        @Override
        public String toString() {
            return "InstanceConnectionImpl{instanceId=" + instanceId + '}';
        }

    }

}
