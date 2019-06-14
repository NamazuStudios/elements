package com.namazustudios.socialengine.remote.jeromq;

import com.google.common.collect.ImmutableSet;
import com.namazustudios.socialengine.rt.InstanceConnectionMonitorService;
import com.namazustudios.socialengine.rt.InstanceConnectionMonitorServiceListener;
import com.namazustudios.socialengine.rt.jeromq.ControlMessageBuilder;
import com.namazustudios.socialengine.rt.jeromq.RouteRepresentationUtil;
import com.namazustudios.socialengine.rt.remote.CommandPreamble;
import com.namazustudios.socialengine.rt.remote.ConnectionService;
import com.namazustudios.socialengine.rt.remote.InstanceUuidListRequest;

import static com.namazustudios.socialengine.rt.remote.CommandPreamble.CommandPreambleFromBytes;
import static com.namazustudios.socialengine.rt.remote.InstanceUuidListRequest.buildInstanceUuidListRequest;
import static com.namazustudios.socialengine.rt.remote.CommandPreamble.CommandType.INSTANCE_UUID_LIST_REQUEST;

import com.namazustudios.socialengine.rt.remote.InstanceUuidListResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import javax.inject.Inject;
import javax.inject.Named;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.namazustudios.socialengine.rt.Constants.*;
import static com.namazustudios.socialengine.rt.remote.InstanceUuidListResponse.InstanceUuidListResponseFromBytes;
import static org.zeromq.ZMQ.REQ;

public class JeroMQInstanceConnectionMonitorService implements InstanceConnectionMonitorService {
    private static final Logger logger = LoggerFactory.getLogger(JeroMQInstanceConnectionMonitorService.class);

    private ConnectionService connectionService;

    private ZContext zContext;

    private int currentInstanceControlPort;

    private final AtomicReference<Set<InstanceConnectionMonitorServiceListener>> atomicListeners = new AtomicReference<>(new HashSet<>());

    private final AtomicReference<Set<UUID>> atomicInstanceUuids = new AtomicReference<>(new HashSet<>());

    private final AtomicReference<Thread> atomicThread = new AtomicReference<>();

    // TODO: inject this
    private int sleepMillis = 1000;

    @Override
    public void start() {
        if (atomicThread.get() != null) {
            throw new IllegalStateException("InstanceConnectionMonitorService is already running.");
        }

        try(
            final ZContext zContext = ZContext.shadow(getzContext());
            final ZMQ.Socket socket = zContext.createSocket(REQ);
            ) {
            final String currentInstanceControlAddress =
                RouteRepresentationUtil.buildTcpAddress("*", getCurrentInstanceControlPort());
            final int sleepMillis = this.sleepMillis;
            socket.connect(currentInstanceControlAddress);

            final Thread thread = new Thread(() -> {
                while (!Thread.interrupted()) {
                    try {
                        final InstanceUuidListRequest instanceUuidListRequest = buildInstanceUuidListRequest();
                        final ZMsg reqZMsg = ControlMessageBuilder.buildControlMsg(
                            INSTANCE_UUID_LIST_REQUEST,
                            instanceUuidListRequest.getByteBuffer()
                        );

                        reqZMsg.send(socket);
                        final ZMsg resZMsg = ZMsg.recvMsg(socket);

                        final byte[] commandPreambleBytes = resZMsg.pop().getData();
                        final CommandPreamble preamble = CommandPreambleFromBytes(commandPreambleBytes);
                        final CommandPreamble.CommandType commandType = preamble.commandType.get();

                        switch (commandType) {
                            case INSTANCE_UUID_LIST_RESPONSE: {
                                final Set<UUID> instanceUuids = StreamSupport
                                    .stream(resZMsg.spliterator(), false)
                                    .map(zFrame -> {
                                        final InstanceUuidListResponse instanceUuidListResponse = InstanceUuidListResponseFromBytes(zFrame.getData());
                                        return instanceUuidListResponse.instanceUuid.get();
                                    })
                                    .collect(Collectors.toSet());

                                synchronized (atomicInstanceUuids) {
                                    atomicInstanceUuids.set(instanceUuids);
                                }


                            }
                                break;
                            default:
                                logger.warn("InstanceConnectionMonitorService: received unhandled command type: {}. Dropping message.", commandType);
                                break;
                        }

                        Thread.sleep(sleepMillis);
                    }
                    catch (InterruptedException e) {
                    }
                }
            });

            synchronized (atomicThread) {
                thread.start();
                atomicThread.compareAndSet(null, thread);
            }
        }
    }

    private List<UUID> retrieveConnectedInstanceUuids() {

    }

    @Override
    public void stop() {
        synchronized (atomicThread) {
            final Thread thread = atomicThread.get();
            thread.interrupt();
            atomicThread.set(null);
        }

        synchronized (atomicInstanceUuids) {
            atomicInstanceUuids.set(null);
        }
    }

    @Override
    public void registerListener(InstanceConnectionMonitorServiceListener listener) {
        synchronized (atomicListeners) {
            final Set<InstanceConnectionMonitorServiceListener> listeners = atomicListeners.get();
            listeners.add(listener);
        }
    }

    @Override
    public boolean unregisterListener(InstanceConnectionMonitorServiceListener listener) {
        synchronized (atomicListeners) {
            final Set<InstanceConnectionMonitorServiceListener> listeners = atomicListeners.get();
            listeners.remove(listener);
        }
    }

    @Override
    public Set<InstanceConnectionMonitorServiceListener> getListeners() {
        return ImmutableSet.copyOf(atomicListeners.get());
    }

    public ConnectionService getConnectionService() {
        return connectionService;
    }

    @Inject
    public void setConnectionService(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    public int getCurrentInstanceControlPort() {
        return currentInstanceControlPort;
    }

    @Inject
    @Named(CURRENT_INSTANCE_CONTROL_PORT_NAME)
    public void setCurrentInstanceControlPort(int currentInstanceControlPort) {
        this.currentInstanceControlPort = currentInstanceControlPort;
    }


    public ZContext getzContext() {
        return zContext;
    }

    @Inject
    public void setzContext(ZContext zContext) {
        this.zContext = zContext;
    }
}
