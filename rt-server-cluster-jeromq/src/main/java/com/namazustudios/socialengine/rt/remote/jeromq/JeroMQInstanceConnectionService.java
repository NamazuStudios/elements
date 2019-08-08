package com.namazustudios.socialengine.rt.remote.jeromq;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.namazustudios.socialengine.rt.InstanceDiscoveryService;
import com.namazustudios.socialengine.rt.InstanceHostInfo;
import com.namazustudios.socialengine.rt.Subscription;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.remote.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Exchanger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;

public class JeroMQInstanceConnectionService implements InstanceConnectionService {

    public static final String BIND_ADDRESS = "com.namazustudios.socialengine.rt.remote.jeromq.bind.addr";

    private static final Logger logger = LoggerFactory.getLogger(JeroMQInstanceConnectionService.class);

    private InstanceId instanceId;

    private ZContext zContext;

    private String bindAddress;

    private Provider<RemoteInvoker> remoteInvokerProvider;

    private InstanceDiscoveryService instanceDiscoveryService;

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
    public InstanceBinding openBinding(final NodeId nodeId) {
        final InstanceConnectionContext context = getContext();
        return context.openBinding(nodeId);
    }

    @Override
    public List<InstanceConnection> getActiveConnections() {
        final InstanceConnectionContext context = getContext();
        return context.getActiveConnections();
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
    public void setBindAddress(@Named(BIND_ADDRESS) String bindAddress) {
        this.bindAddress = bindAddress;
    }

    public Provider<RemoteInvoker> getRemoteInvokerProvider() {
        return remoteInvokerProvider;
    }

    @Inject
    public void setRemoteInvokerProvider(Provider<RemoteInvoker> remoteInvokerProvider) {
        this.remoteInvokerProvider = remoteInvokerProvider;
    }

    public InstanceDiscoveryService getInstanceDiscoveryService() {
        return instanceDiscoveryService;
    }

    @Inject
    public void setInstanceDiscoveryService(InstanceDiscoveryService instanceDiscoveryService) {
        this.instanceDiscoveryService = instanceDiscoveryService;
    }

    private class InstanceConnectionContext {

        private Thread server;

        private Subscription onDiscover;

        private Subscription onUndisover;

        private final Exchanger<Exception> exceptionExchanger = new Exchanger<>();

        private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

        private final String internalBindAddress = format("inproc://control/%s", randomUUID());

        private final BiMap<InstanceHostInfo, JeroMQInstanceConnection> activeConnections = HashBiMap.create();

        private final BiMap<JeroMQInstanceConnection, InstanceHostInfo> rActiveConnections = activeConnections.inverse();

        private final Publisher<InstanceConnection> onConnect = new ConcurrentLockedPublisher<>(readWriteLock.writeLock());

        private final Publisher<InstanceConnection> onDisconnect = new ConcurrentLockedPublisher<>(readWriteLock.writeLock());

        public void start() {

            onDiscover = getInstanceDiscoveryService().subscribeToDiscovery(i -> getOrCreateNewConnection(i));
            onUndisover = getInstanceDiscoveryService().subscribeToUndiscovery(i -> disconnect(i));
            server = new Thread(this::server);
            server.setDaemon(true);
            server.setName(JeroMQInstanceConnectionService.class.getSimpleName() + " server.");
            server.setUncaughtExceptionHandler((t, ex) -> logger.error("Error running InstanceConnectionService", ex));
            server.start();

            try {
                final Exception ex = exchangeException(null);
                if (ex != null) throw ex;
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new InternalException(ex);
            }

            getInstanceDiscoveryService().getKnownHosts().forEach(this::createNewConnection);

        }

        public void stop() {

            onDiscover.unsubscribe();
            onUndisover.unsubscribe();
            drain();

            try {
                server.interrupt();
                server.join();
            } catch (InterruptedException e) {
                throw new InternalException(e);
            }

        }

        private void server() {

            final List<String> binds = asList(getInternalBindAddress(), getBindAddress());

            try (final JeroMQRoutingServer server = new JeroMQRoutingServer(getInstanceId(), getzContext(), binds)) {
                exchangeException(null);
                server.run();
            } catch (Exception ex) {
                logger.error("Exception starting up the routing server.", ex);
                exchangeException(ex);
            }

        }

        private Exception exchangeException(final Exception ex) {
            try {
                return exceptionExchanger.exchange(ex);
            } catch (InterruptedException e) {
                logger.error("Interrupted exchaning exceptions to calling thread.", e);
                throw new InternalException(e);
            }
        }

        public String getInternalBindAddress() {
            return internalBindAddress;
        }

        public List<InstanceConnection> getActiveConnections() {

            final Lock rLock = readWriteLock.readLock();

            try {
                rLock.lock();
                return activeConnections.values().stream().collect(toList());
            } finally {
                rLock.unlock();
            }

        }

        public InstanceConnection getOrCreateNewConnection(final InstanceHostInfo instanceHostInfo) {

            final Lock rLock = readWriteLock.readLock();

            try {
                rLock.lock();
                final JeroMQInstanceConnection connection = activeConnections.get(instanceHostInfo);
                if (connection != null) return connection;
            } finally {
                rLock.unlock();
            }

            return createNewConnection(instanceHostInfo);

        }

        private JeroMQInstanceConnection createNewConnection(final InstanceHostInfo instanceHostInfo) {

            final InstanceId instanceId;
            final String instanceConnectAddress = instanceHostInfo.getConnectAddress();

            try (final ControlClient client = new JeroMQControlClient(getzContext(), instanceConnectAddress)) {
                final InstanceStatus status = client.getInstanceStatus();
                instanceId = status.getInstanceId();
            }

            final Lock wLock = readWriteLock.writeLock();

            try {

                JeroMQInstanceConnection connection;

                wLock.lock();
                connection = activeConnections.get(instanceHostInfo);
                if (connection != null) return connection;

                final RemoteInvoker remoteInvoker = getRemoteInvokerProvider().get();
                remoteInvoker.start(instanceConnectAddress);

                connection = new JeroMQInstanceConnection(
                        instanceId,
                        remoteInvoker,
                        getzContext(),
                        getInternalBindAddress(),
                        instanceHostInfo,
                        this::disconnect);

                activeConnections.put(instanceHostInfo, connection);
                onConnect.publishAsync(connection);

                return connection;

            } finally {
                wLock.unlock();
            }

        }

        public void drain() {
            final Lock wLock = readWriteLock.writeLock();

            try {
                wLock.lock();

                final List<InstanceConnection> connectionList = new ArrayList<>(activeConnections.values());

                connectionList.forEach(c -> {
                    try {
                        c.disconnect();
                    } catch (Exception ex) {
                        logger.error("Could not drain connection {}", ex);
                    }
                });

            } finally {
                wLock.unlock();
            }

        }

        public void disconnect(final InstanceHostInfo instanceHostInfo) {
            final Lock wLock = readWriteLock.writeLock();

            try {
                wLock.lock();
                final InstanceConnection connection = activeConnections.remove(instanceHostInfo);
                connection.disconnect();
            } finally {
                wLock.unlock();
            }

        }

        public void disconnect(final JeroMQInstanceConnection connection) {

            final Lock wLock = readWriteLock.writeLock();

            try {
                wLock.lock();

                final InstanceHostInfo address = rActiveConnections.remove(connection);
                if (address == null) return;

                logger.info("Disconnected from instance {}", address);
                onDisconnect.publishAsync(connection, c -> connection.getRemoteInvoker().stop());

            } finally {
                wLock.unlock();
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
               final InstanceBinding instanceBinding = client.openBinding(nodeId);
               return instanceBinding;
            }
        }

    }

}
