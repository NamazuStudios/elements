package com.namazustudios.socialengine.rt.remote.jeromq;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.namazustudios.socialengine.rt.AsyncPublisher;
import com.namazustudios.socialengine.rt.ConcurrentLockedPublisher;
import com.namazustudios.socialengine.rt.Subscription;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.remote.*;
import com.namazustudios.socialengine.rt.remote.AsyncControlClient.Request;
import com.namazustudios.socialengine.rt.util.ReadWriteGuard;
import com.namazustudios.socialengine.rt.util.ReentrantReadWriteGuard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.namazustudios.socialengine.rt.id.NodeId.forMasterNode;
import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toUnmodifiableList;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;

public class JeroMQInstanceConnectionService implements InstanceConnectionService {

    public static final String JEROMQ_CLUSTER_BIND_ADDRESS =
        "com.namazustudios.socialengine.rt.remote.jeromq.bind.addr";

    public static final String JEROMQ_CONNECTION_SERVICE_REFRESH_INTERVAL_SECONDS =
        "com.namazustudios.socialengine.rt.remote.jeromq.connection.service.refresh.interval.sec";

    private static final long REPORT_INTERVAL_SECONDS = 15;

    private static final long REFRESH_INTERVAL_SECONDS = 5;

    private static final long DEFAULT_REFRESH_TIME_OUT_SECONDS = 5;

    private static final Logger logger = LoggerFactory.getLogger(JeroMQInstanceConnectionService.class);

    private InstanceId instanceId;

    private ZContext zContext;

    private String bindAddress;

    private long refreshIntervalInSeconds;

    private Provider<RemoteInvoker> remoteInvokerProvider;

    private InstanceDiscoveryService instanceDiscoveryService;

    private AsyncControlClient.Factory asyncControlClientFactory;

    private final AtomicReference<InstanceConnectionContext> context = new AtomicReference<>();

    @Override
    public void start() {

        final InstanceConnectionContext context = new InstanceConnectionContext();

        if (this.context.compareAndSet(null, context)) {
            context.start();
        } else {
            throw new IllegalStateException("Already started.");
        }

    }

    @Override
    public void stop() {

        final InstanceConnectionContext context = this.context.getAndSet(null);

        if (context == null) {
            throw new IllegalStateException("Not running.");
        } else {
            context.stop();
        }

    }

    @Override
    public void refresh() {
        final var context = getContext();
        context.refresh(DEFAULT_REFRESH_TIME_OUT_SECONDS, SECONDS);
    }

    @Override
    public InstanceBinding openBinding(final NodeId nodeId) {
        final InstanceConnectionContext context = getContext();
        return context.openBinding(nodeId);
    }

    @Override
    public List<InstanceConnection> getActiveConnections() {
        final InstanceConnectionContext context = getContext();
        return context.getActive();
    }

    @Override
    public Subscription subscribeToConnect(final Consumer<InstanceConnection> onConnect) {
        final InstanceConnectionContext context = getContext();
        return context.subscribeToConnect(onConnect);
    }

    @Override
    public Subscription subscribeToDisconnect(final Consumer<InstanceConnection> onDisconnect) {
        final InstanceConnectionContext context = getContext();
        return context.subscribeToDisconnect(onDisconnect);
    }

    @Override
    public String getLocalControlAddress() {
        final InstanceConnectionContext context = getContext();
        return context.getInternalBindAddress();
    }

    private InstanceConnectionContext getContext() {
        final InstanceConnectionContext context = this.context.get();
        if (context == null) throw new IllegalStateException("Not running.");
        return context;
    }

    @Override
    public InstanceId getInstanceId() {
        return instanceId;
    }

    @Inject
    public void setInstanceId(InstanceId instanceId) {
        this.instanceId = instanceId;
    }

    public ZContext getzContext() {
        return zContext;
    }

    @Inject
    public void setzContext(ZContext zContext) {
        this.zContext = zContext;
    }

    public String getBindAddress() {
        return bindAddress;
    }

    @Inject
    public void setBindAddress(@Named(JEROMQ_CLUSTER_BIND_ADDRESS) String bindAddress) {
        this.bindAddress = bindAddress;
    }

    public Provider<RemoteInvoker> getRemoteInvokerProvider() {
        return remoteInvokerProvider;
    }

    @Inject
    public void setRemoteInvokerProvider(Provider<RemoteInvoker> remoteInvokerProvider) {
        this.remoteInvokerProvider = remoteInvokerProvider;
    }

    public long getRefreshIntervalInSeconds() {
        return refreshIntervalInSeconds;
    }

    @Inject
    public void setRefreshIntervalInSeconds(@Named(JEROMQ_CONNECTION_SERVICE_REFRESH_INTERVAL_SECONDS) long refreshIntervalInSeconds) {
        this.refreshIntervalInSeconds = refreshIntervalInSeconds;
    }

    public InstanceDiscoveryService getInstanceDiscoveryService() {
        return instanceDiscoveryService;
    }

    @Inject
    public void setInstanceDiscoveryService(InstanceDiscoveryService instanceDiscoveryService) {
        this.instanceDiscoveryService = instanceDiscoveryService;
    }

    public AsyncControlClient.Factory getAsyncControlClientFactory() {
        return asyncControlClientFactory;
    }

    @Inject
    public void setAsyncControlClientFactory(AsyncControlClient.Factory asyncControlClientFactory) {
        this.asyncControlClientFactory = asyncControlClientFactory;
    }

    private class InstanceConnectionContext {

        private Thread server;

        private Subscription onDiscover;

        private Subscription onUndiscover;

        private AsyncControlClient localControlClient;

        private ScheduledFuture<?> refreshScheduledFuture;

        private final AtomicBoolean running = new AtomicBoolean(true);

        private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, r -> {
            final Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName(JeroMQInstanceConnectionService.class.getSimpleName() + " refresher.");
            thread.setUncaughtExceptionHandler((t, ex) -> logger.error("Error in scheduler.", ex));
            return thread;
        });

        private final Exchanger<Exception> exceptionExchanger = new Exchanger<>();

        private ReadWriteGuard rwGuard = new ReentrantReadWriteGuard();

        private final String internalBindAddress = format("inproc://control/%s", instanceId);

        private final Map<InstanceHostInfo, Request> pending = new HashMap<>();

        private final BiMap<InstanceHostInfo, JeroMQInstanceConnection> active = HashBiMap.create();

        private final BiMap<JeroMQInstanceConnection, InstanceHostInfo> rActiveConnections = active.inverse();

        private final AsyncPublisher<InstanceConnection> onConnect = new ConcurrentLockedPublisher<>(rwGuard.getWriteLock(), scheduler::submit);

        private final AsyncPublisher<InstanceConnection> onDisconnect = new ConcurrentLockedPublisher<>(rwGuard.getWriteLock(), scheduler::submit);

        public void start() {

            localControlClient = getAsyncControlClientFactory()
                .open(getInternalBindAddress())
                .withDispatch(scheduler::submit);

            onDiscover = getInstanceDiscoveryService().subscribeToDiscovery(this::createNewConnectionIfAbsent);
            onUndiscover = getInstanceDiscoveryService().subscribeToUndiscovery(this::disconnect);

            // Since the event publish may mutate the connection and it is always done within the context of the
            // writer lock, we should signal all waiters while the event pumps.

            server = new Thread(this::server);
            server.setDaemon(true);
            server.setName(JeroMQInstanceConnectionService.class.getSimpleName() + " server.");
            server.setUncaughtExceptionHandler((t, ex) -> logger.error("Error running InstanceConnectionService", ex));
            server.start();

            refreshScheduledFuture = scheduler.scheduleAtFixedRate(this::refreshAsync, 0, REFRESH_INTERVAL_SECONDS, SECONDS);

            try {
                final Exception ex = exchangeException(null);
                if (ex != null) throw ex;
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new InternalException(ex);
            }

            scheduler.scheduleWithFixedDelay(
                this::logStatus,
                0,
                REPORT_INTERVAL_SECONDS,
                SECONDS);

        }

        private void logStatus() {

            if (logger.isInfoEnabled()) {

                final var known = getInstanceDiscoveryService()
                    .getKnownHosts()
                    .stream()
                    .map(Object::toString)
                    .collect(joining(","));

                final var active = rwGuard.computeRO(c -> this.active
                    .keySet()
                    .stream()
                    .map(Object::toString)
                    .collect(joining(","))
                );

                final var pending = rwGuard.computeRO(c -> this.pending
                    .keySet()
                    .stream()
                    .map(Object::toString)
                    .collect(joining(","))
                );

                logger.info("\nKnown Hosts [{}]\nPending[{}]\nActive [{}]", known, pending, active);

            }

        }

        public void createNewConnectionIfAbsent(final InstanceHostInfo instanceHostInfo) {
            try {

                final var create = rwGuard.computeRO(c ->
                    !pending.containsKey(instanceHostInfo) &&
                    !active.containsKey(instanceHostInfo)
                );

                if (create) createNewConnection(instanceHostInfo);

            } catch (Exception ex) {
                logger.error("Caught exception creating host from address {}",
                             instanceHostInfo.getConnectAddress(), ex);
            }
        }

        private void createNewConnection(final InstanceHostInfo instanceHostInfo) {
            rwGuard.rw(condition -> pending.compute(instanceHostInfo, (nfo, existing) -> {

                // If an existing request exists, then we simply return it. No need to mutate the map
                if (existing != null) return existing;

                // If not, then we must create a client. THe client will be passed through the
                // rest of the refresh process for this particular instance host information bit.

                // Note, we delegate the scheduler pool to handle the actual request/response business so
                // the server's core IO threads are needlessly busy with executions.

                final var instanceConnectAddress = nfo.getConnectAddress();
                final var rClient = getAsyncControlClientFactory()
                    .open(instanceConnectAddress)
                    .withDispatch(scheduler::execute);

                // Now that we have an async client, we can interrogate the remote end to get the instance id of the
                // node and formally establish the route to the node. This way, we know the node ID and can associate
                // its IP/endpoint address.

                logger.info("Fetching instance status for {}", instanceHostInfo);

                return rClient.getInstanceStatus(response -> {

                    final InstanceStatus status;

                    try {
                        // We first attempt to get the status from the socket.
                        status = response.get();
                        logger.info("Got status. {} -> {}", instanceHostInfo, status);
                    } catch (Exception ex) {
                        // If that fails we log it, close the client, as well as use the write lock to both remove
                        // the pending request. We also return here to bail out from processing further.
                        logger.info("Failed to get instance status from {}. Closing.", instanceConnectAddress);
                        rClient.close();
                        rwGuard.rw(_condition -> pending.remove(nfo));
                        return;
                    }

                    // We successfully got the status, therefore we must open the route to the master node based on
                    // the instance status.

                    openRouteToMasterNode(nfo, status, rClient);

                });

            }));
        }

        private void openRouteToMasterNode(final InstanceHostInfo instanceHostInfo,
                                           final InstanceStatus instanceStatus,
                                           final AsyncControlClient rClient) {

            // We generate the appropriate ids based on the instance IDs that are returned from the status.

            final var instanceId = instanceStatus.getInstanceId();
            final var masterNodeId = forMasterNode(instanceId);
            final var instanceConnectAddress = instanceHostInfo.getConnectAddress();

            logger.info("Opening route to master node @{}", instanceHostInfo);

            // Knowing the host information, we use the local control client to tell the local routing server to connect
            // to the instance. This opens up the route to the node such that this instance may see it and other clients
            // may connect to the master node to interrogate it for information on the hosted nodes.

            final var request = localControlClient.openRouteToNode(
                    masterNodeId,
                    instanceConnectAddress,
                    response -> {

                final String masterNodeConnectAddress;

                try {
                    // We attempt to fetch the newly created connect address. This can be used to create the permanent
                    // connection through this server.
                    masterNodeConnectAddress = response.get();
                    logger.info("Obtained master node connect address {}", masterNodeConnectAddress);

                    // Finally, we take what was processed, and we add it to the internal collection pool.
                    addInstanceConnection(instanceHostInfo, instanceStatus, masterNodeConnectAddress);

                } catch (Exception ex) {
                    logger.warn("Failed to open route to master node {} -> {}", masterNodeId, instanceConnectAddress);
                    rwGuard.rw(condition -> pending.remove(instanceHostInfo));
                } finally {
                    // Regardless, the remote client we made to do the direct connection is no longer needed, so we
                    // close it out. Future connections will be made via the internal routing server so this connection
                    // is not really needed anymore.
                    rClient.close();
                }

            });

            // Replace the current request with the pending request.
            rwGuard.rw(condition -> pending.put(instanceHostInfo, request));

        }

        private void addInstanceConnection(final InstanceHostInfo instanceHostInfo,
                                           final InstanceStatus instanceStatus,
                                           final String masterNodeConnectAddress) {

            // The following must both be done in the same lock because it should remove from pending and put into
            // active at the same time.
            final var connection = rwGuard.computeRW(condition -> {

                // Removes the pending connection by just removing it from the map.
                pending.remove(instanceHostInfo);

                // Finally this computes the new connection. Practically speaking this should not ever happen, but we
                // add some checks to ensure that this is correctly processed.

                return active.compute(instanceHostInfo, (nfo, existing) -> {

                    logger.info("Activating connection for {}", nfo);

                    if (existing != null) return existing;

                    final RemoteInvoker remoteInvoker = getRemoteInvokerProvider().get();
                    remoteInvoker.start(masterNodeConnectAddress);

                    return new JeroMQInstanceConnection(
                        instanceStatus.getInstanceId(),
                        remoteInvoker,
                        getzContext(),
                        getInternalBindAddress(),
                        nfo,
                        this::disconnect
                    );

                });

            });

            // Finally, we publish the connection to all listeners who are interested in the update.
            onConnect.publishAsync(connection, c -> rwGuard.getCondition().signalAll());

        }

        public void stop() {

            onDiscover.unsubscribe();
            onUndiscover.unsubscribe();
            refreshScheduledFuture.cancel(true);
            drain();

            localControlClient.close();

            try {
                scheduler.shutdown();
                scheduler.awaitTermination(1, MINUTES);
            } catch (InterruptedException e) {
                logger.error("Interrupted while shutting down.", e);
            }

            try {
                running.set(false);
                server.interrupt();
                server.join();
            } catch (InterruptedException e) {
                throw new InternalException(e);
            }

        }

        public void refresh(final long time, final TimeUnit timeUnit) {
            rwGuard.rw(condition -> {

                // Finds all unknown hosts and disconnects them.

                final var known = refreshAsync();

                try {
                    while (!active.keySet().containsAll(known)) {
                        if (!condition.await(time, timeUnit)) {
                            throw new InternalException("Timed out after " + time + " " + timeUnit);
                        }
                    }
                } catch (InterruptedException e) {
                    logger.info("Interrupted refreshing.", e);
                }

                logger.info("Refreshed successfully.");

            });
        }

        public Collection<InstanceHostInfo> refreshAsync() {
            return rwGuard.computeRW(condition -> {
                final var known = getInstanceDiscoveryService().getKnownHosts();
                final var toAdd = new HashSet<>(known);

                toAdd.removeAll(active.keySet());
                toAdd.removeAll(pending.keySet());
                toAdd.forEach(this::createNewConnection);

                return known;
            });
        }

        private void server() {

            final var binds = of(getInternalBindAddress(), getBindAddress())
                .filter(addr -> !addr.isBlank())
                .collect(toUnmodifiableList());

            try (final JeroMQRoutingServer server = new JeroMQRoutingServer(getInstanceId(), getzContext(), binds)) {
                final var incoming = exchangeException(null);
                assert incoming == null;
                server.run(running::get);
                logger.info("Got signal to stop running.  Shutting down IO thread.");
            } catch (Exception ex) {
                logger.error("Exception running the routing server.", ex);
                final var incoming = exchangeException(ex);
                assert incoming == null;
            }

        }

        private Exception exchangeException(final Exception ex) {
            try {
                return exceptionExchanger.exchange(ex);
            } catch (InterruptedException e) {
                logger.error("Interrupted exchanging exceptions to calling thread.", e);
                throw new InternalException(e);
            }
        }

        public String getInternalBindAddress() {
            return internalBindAddress;
        }

        public List<InstanceConnection> getActive() {
            return rwGuard.computeRO(c -> new ArrayList<>(active.values()));
        }

        public void drain() {

            rwGuard.rw(condition -> {

                pending.values().forEach(Request::cancel);
                pending.clear();

                active.values().forEach(onDisconnect::publishAsync);
                active.clear();

                condition.signalAll();

            });

        }

        public void disconnect(final InstanceHostInfo instanceHostInfo) {

            final var connection = rwGuard.computeRW(condition -> {

                final var c = active.remove(instanceHostInfo);

                if (c == null) {
                    logger.debug("Connection not active. Removing.");
                } else {
                    localControlClient.closeRoutesViaInstance(
                        c.getInstanceId(),
                        instanceHostInfo.getConnectAddress(),
                        response -> logger.info("Closed routes via {}", instanceId)
                    );
                }

                final var request = pending.remove(instanceHostInfo);

                if (request == null) {
                    logger.debug("No pending request for {}. Skipping.", instanceHostInfo);
                } else {
                    request.cancel();
                }

                return c;

            });

            if (connection == null) {
                logger.info("Connection for host {} wasn't removed.", instanceHostInfo.getConnectAddress());
            } else {

                logger.info("Disconnected from instance {}", instanceHostInfo.getConnectAddress());

                onDisconnect.publishAsync(connection, c -> {
                    rwGuard.getCondition().signalAll();
                    connection.getRemoteInvoker().stop();
                });

            }

        }

        public void disconnect(final JeroMQInstanceConnection connection) {

            final var publish = rwGuard.computeRW(condition -> {

                final var instanceHostInfo = rActiveConnections.remove(connection);
                if (instanceHostInfo == null) return false;

                localControlClient.closeRoutesViaInstance(
                    connection.getInstanceId(),
                    instanceHostInfo.getConnectAddress(),
                    response -> logger.info("Closed routes via {}", instanceId)
                );

                return true;

            });

            if (publish) {
                onDisconnect.publishAsync(connection, c -> {
                    rwGuard.getCondition().signalAll();
                    connection.getRemoteInvoker().stop();
                });
            }

        }

        public Subscription subscribeToConnect(final Consumer<InstanceConnection> onConnect) {
            return this.onConnect.subscribe(onConnect);
        }

        public Subscription subscribeToDisconnect(final Consumer<InstanceConnection> onDisconnect) {
            return this.onDisconnect.subscribe(onDisconnect);
        }

        public InstanceBinding openBinding(final NodeId nodeId) {
            try (final ControlClient client = new JeroMQControlClient(getzContext(), getInternalBindAddress())) {
                return client.openBinding(nodeId);
            }
        }

    }

}
