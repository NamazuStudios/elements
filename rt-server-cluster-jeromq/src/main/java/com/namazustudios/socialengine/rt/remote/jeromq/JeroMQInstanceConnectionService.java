package com.namazustudios.socialengine.rt.remote.jeromq;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.namazustudios.socialengine.rt.InstanceDiscoveryService;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.remote.InstanceConnectionService;
import com.namazustudios.socialengine.rt.remote.PubSub;
import com.namazustudios.socialengine.rt.remote.RemoteInvoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.util.List;
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

    public static final String EXTERNAL_INVOKER_ADDRESS = "com.namazustudios.socialengine.rt.remote.jeromq.invoker.addr";

    public static final String EXTERNAL_CONTROL_ADDRESS = "com.namazustudios.socialengine.rt.remote.jeromq.server.addr";

    private static final Logger logger = LoggerFactory.getLogger(JeroMQInstanceConnectionService.class);

    private ZContext zContext;

    private String externalControlAddress;

    private String externalInvokerAddress;

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
    public InstanceConnection connectToInstance(final String remoteAddress) {
        final InstanceConnectionContext context = getContext();
        return context.getOrCreateNewConnection(remoteAddress);
    }

    @Override
    public List<InstanceConnection> getActiveConnections() {
        final InstanceConnectionContext context = getContext();
        return context.getActiveConnections();
    }

    @Override
    public PubSub.Subscription subscribeToConnect(final Consumer<InstanceConnection> onConnect) {
        final InstanceConnectionContext context = getContext();
        return context.subscribeToConnect(onConnect);
    }

    @Override
    public PubSub.Subscription subscribeToDisconnect(final Consumer<InstanceConnection> onDisconnect) {
        final InstanceConnectionContext context = getContext();
        return context.subscribeToDisconnect(onDisconnect);
    }

    private InstanceConnectionContext getContext() {
        final InstanceConnectionContext context = this.context.get();
        if (context == null) throw new IllegalStateException("Not running.");
        return context;
    }

    public ZContext getzContext() {
        return zContext;
    }

    @Inject
    public void setzContext(ZContext zContext) {
        this.zContext = zContext;
    }

    public String getExternalControlAddress() {
        return externalControlAddress;
    }

    @Inject
    public void setExternalControlAddress(@Named(EXTERNAL_CONTROL_ADDRESS) String externalControlAddress) {
        this.externalControlAddress = externalControlAddress;
    }

    public Provider<RemoteInvoker> getRemoteInvokerProvider() {
        return remoteInvokerProvider;
    }

    @Inject
    public void setRemoteInvokerProvider(Provider<RemoteInvoker> remoteInvokerProvider) {
        this.remoteInvokerProvider = remoteInvokerProvider;
    }

    public String getExternalInvokerAddress() {
        return externalInvokerAddress;
    }

    @Inject
    public void setExternalInvokerAddress(@Named(EXTERNAL_INVOKER_ADDRESS) String externalInvokerAddress) {
        this.externalInvokerAddress = externalInvokerAddress;
    }

    public InstanceDiscoveryService getInstanceDiscoveryService() {
        return instanceDiscoveryService;
    }

    @Inject
    public void setInstanceDiscoveryService(@Named(EXTERNAL_CONTROL_ADDRESS) InstanceDiscoveryService instanceDiscoveryService) {
        this.instanceDiscoveryService = instanceDiscoveryService;
    }

    private class InstanceConnectionContext {

        private Thread server;

        private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

        private final String internalControlAddress = format("inproc://server/%s", randomUUID());

        private final BiMap<String, JeroMQInstanceConnection> activeConnections = HashBiMap.create();

        private final BiMap<JeroMQInstanceConnection, String> rActiveConnections = activeConnections.inverse();

        private final PubSub<InstanceConnection> onConnect = new PubSub<>(readWriteLock.writeLock());

        private final PubSub<InstanceConnection> onDisconnect = new PubSub<>(readWriteLock.writeLock());

        public void start() {
            server = new Thread(this::server);
            server.setDaemon(true);
            server.setName(JeroMQInstanceConnectionService.class.getName() + " server.");
            server.setUncaughtExceptionHandler((t, ex) -> logger.error("Error running InstanceConnectionService", ex));
            server.start();
        }

        public void stop() {

            server.interrupt();

            try {
                server.join();
            } catch (InterruptedException e) {
                throw new InternalException(e);
            }

        }

        private void server() {

            final List<String> invokers = asList(getExternalInvokerAddress());
            final List<String> controls = asList(getInternalControlAddress(), getExternalControlAddress());

            try (final JeroMQRoutingServer server = new JeroMQRoutingServer(getzContext(), controls, invokers)) {
                getInstanceDiscoveryService().getRemoteConnections().forEach(this::createNewConnection);
                server.run();
            }

        }

        public String getInternalControlAddress() {
            return internalControlAddress;
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

        public InstanceConnection getOrCreateNewConnection(final String instanceRemoteAddress) {

            final Lock rLock = readWriteLock.readLock();

            try {
                rLock.lock();
                final JeroMQInstanceConnection connection = activeConnections.get(instanceRemoteAddress);
                if (connection != null) return connection;
            } finally {
                rLock.unlock();
            }

            return createNewConnection(instanceRemoteAddress);

        }

        private JeroMQInstanceConnection createNewConnection(final String instanceRemoteAddress) {

            final InstanceId instanceId;

            try (final JeroMQControlClient client = new JeroMQControlClient(getzContext(), instanceRemoteAddress)) {
                final JeroMQInstanceStatus status = client.getInstanceStatus();
                instanceId = status.getInstanceId();
            }

            final Lock wLock = readWriteLock.writeLock();

            try {

                JeroMQInstanceConnection connection;

                wLock.lock();
                connection = activeConnections.get(instanceRemoteAddress);
                if (connection != null) return connection;

                final RemoteInvoker remoteInvoker = getRemoteInvokerProvider().get();
                remoteInvoker.start(instanceRemoteAddress);

                connection = new JeroMQInstanceConnection(
                        instanceId,
                        remoteInvoker,
                        getzContext(),
                        getInternalControlAddress(),
                        this::disconnect);

                activeConnections.put(instanceRemoteAddress, connection);
                onConnect.publishAsync(connection);

                return connection;

            } finally {
                wLock.unlock();
            }

        }

        private void disconnect(final JeroMQInstanceConnection connection) {

            final Lock wLock = readWriteLock.writeLock();

            try {
                wLock.lock();

                final String address = rActiveConnections.remove(connection);
                if (address == null) return;

                logger.info("Disconnected from instance {}", address);
                onDisconnect.publishAsync(connection, c -> connection.getRemoteInvoker().stop());

            } finally {
                wLock.unlock();
            }

        }

        public PubSub.Subscription subscribeToConnect(final Consumer<InstanceConnection> onConnect) {
            return this.onConnect.subscribe(onConnect);
        }

        public PubSub.Subscription subscribeToDisconnect(final Consumer<InstanceConnection> onDisconnect) {
            return this.onDisconnect.subscribe(onDisconnect);
        }

    }

}
