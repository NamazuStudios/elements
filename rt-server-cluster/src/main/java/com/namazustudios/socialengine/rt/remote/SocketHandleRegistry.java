package com.namazustudios.socialengine.rt.remote;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.namazustudios.socialengine.rt.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

import static com.namazustudios.socialengine.rt.Constants.CURRENT_INSTANCE_UUID_NAME;

/**
 * Maps socket handles to tcp addresses/inproc identifiers (and vice versa). Note: this is not a thread-safe module and
 * is meant to be accessed only within a connection runnable thread.
 */
public class SocketHandleRegistry implements AutoCloseable {

    public static final int SOCKET_HANDLE_NOT_FOUND = -1;

    private static final Logger logger = LoggerFactory.getLogger(SocketHandleRegistry.class);

    // TODO: have higher-level registry where other layers can inspect instance uuid <-> nodeids

    // TODO: expected sizes from env vars
    private String boundInvokerTcpAddress = null;
    private int boundInvokerSocketHandle = SOCKET_HANDLE_NOT_FOUND;

    private final BiMap<String, Integer> invokerTcpAddressToSocketHandle = new HashBiMap<>(16);
    private final BiMap<String, Integer> controlTcpAddressToSocketHandle = new HashBiMap<>(16);

    private final BiMap<Integer, Integer> controlSocketHandleToInvokerSocketHandle = new HashBiMap<>(16);

    private final BiMap<UUID, Integer> instanceUuidToInvokerSocketHandle = new HashBiMap<>(16);
    private final BiMap<UUID, Integer> instanceUuidToControlSocketHandle = new HashBiMap<>(16);

    private final BiMap<NodeId, Integer> nodeIdToSocketHandle = new HashBiMap<>(16);

    private final Map<UUID, Set<NodeId>> instanceUuidToNodeIds = new HashMap<>(16);

    private final Map<UUID, Set<Integer>> instanceUuidToNodeSocketHandles = new HashMap<>(16);

    private Set<NodeId> getOrCreateNodeIdSetForInstanceUuid(final UUID instanceUuid) {
        return instanceUuidToNodeIds.computeIfAbsent(instanceUuid, i -> new HashSet<>());
    }

    private Set<Integer> getOrCreateNodeSocketHandleSetForInstanceUuid(final UUID instanceUuid) {
        return instanceUuidToNodeSocketHandles.computeIfAbsent(instanceUuid, i -> new HashSet<>());
    }

    private static int returnSocketHandleNotFoundIfNecessary(final Integer socketHandle) {
        if (socketHandle != null) {
            return socketHandle;
        }
        else {
            return SOCKET_HANDLE_NOT_FOUND;
        }
    }

    public boolean hasInvokerTcpAddress(final String invokerTcpAddress) {
        return invokerTcpAddressToSocketHandle.containsKey(invokerTcpAddress);
    }

    public boolean hasControlTcpAddress(final String controlTcpAddress) {
        return controlTcpAddressToSocketHandle.containsKey(controlTcpAddress);
    }

    public boolean hasInstanceUuid(final UUID instanceUuid) {
        return instanceUuidToInvokerSocketHandle.containsKey(instanceUuid);
    }

    public int getSocketHandleForInvokerTcpAddress(final String invokerTcpAddress) {
        return returnSocketHandleNotFoundIfNecessary(invokerTcpAddressToSocketHandle.get(invokerTcpAddress));
    }

    public String getInvokerTcpAddressForSocketHandle(final int socketHandle) {
        return invokerTcpAddressToSocketHandle.inverse().get(socketHandle);
    }

    public int getSocketHandleForControlTcpAddress(final String controlTcpAddress) {
        return returnSocketHandleNotFoundIfNecessary(controlTcpAddressToSocketHandle.get(controlTcpAddress));
    }

    public String getControlTcpAddressForSocketHandle(final int socketHandle) {
        return controlTcpAddressToSocketHandle.inverse().get(socketHandle);
    }

    public int getInvokerSocketHandleForControlSocketHandle(final int controlSocketHandle) {
        return returnSocketHandleNotFoundIfNecessary(controlSocketHandleToInvokerSocketHandle.get(controlSocketHandle));
    }

    public int getControlSocketHandleForInvokerSocketHandle(final int invokerSocketHandle) {
        return returnSocketHandleNotFoundIfNecessary(controlSocketHandleToInvokerSocketHandle.inverse().get(invokerSocketHandle));
    }

    public int getInvokerSocketHandleForInstanceUuid(final UUID instanceUuid) {
        return returnSocketHandleNotFoundIfNecessary(instanceUuidToInvokerSocketHandle.get(instanceUuid));
    }

    public UUID getInstanceUuidForInvokerSocketHandle(final int invokerSocketHandle) {
        return instanceUuidToInvokerSocketHandle.inverse().get(invokerSocketHandle);
    }

    public int getControlSocketHandleForInstanceUuid(final UUID instanceUuid) {
        return returnSocketHandleNotFoundIfNecessary(instanceUuidToControlSocketHandle.get(instanceUuid));
    }

    public UUID getInstanceUuidForControlSocketHandle(final int controlSocketHandle) {
        return instanceUuidToControlSocketHandle.inverse().get(controlSocketHandle);
    }

    public int getSocketHandleForNodeId(final NodeId nodeId) {
        return returnSocketHandleNotFoundIfNecessary(nodeIdToSocketHandle.get(nodeId));
    }

    public NodeId getNodeIdForSocketHandle(final int socketHandle) {
        return nodeIdToSocketHandle.inverse().get(socketHandle);
    }

    public Set<Integer> getNodeSocketHandlesForInstanceUuid(final UUID instanceUuid) {
        return instanceUuidToNodeSocketHandles.get(instanceUuid);
    }

    public Set<UUID> getRegisteredInstanceUuids() {
        return instanceUuidToInvokerSocketHandle.keySet();
    }

    public String getBoundInvokerTcpAddress() {
        return boundInvokerTcpAddress;
    }

    public int getBoundInvokerSocketHandle() {
        return boundInvokerSocketHandle;
    }

    /**
     * Instance registration is a two-step procedure: first, the socket handles and their corresponding tcp addresses
     * must be registered. Then, after the control conn receives the {@link StatusResponse}, the Instance UUID found
     * within the StatusResponse must then be registered using {@link SocketHandleRegistry#registerInstanceUuid}.
     *
     * @param invokerTcpAddress
     * @param invokerSocketHandle
     * @param controlTcpAddress
     * @param controlSocketHandle
     */
    public void registerInstanceSocketHandles(
        final String invokerTcpAddress,
        final int invokerSocketHandle,
        final String controlTcpAddress,
        final int controlSocketHandle
    ) {
        invokerTcpAddressToSocketHandle.put(invokerTcpAddress, invokerSocketHandle);
        controlTcpAddressToSocketHandle.put(controlTcpAddress, controlSocketHandle);
        controlSocketHandleToInvokerSocketHandle.put(controlSocketHandle, invokerSocketHandle);
    }

    /**
     * The final step of the two-step instance registration procedure.
     *
     * @param controlSocketHandle
     * @param instanceUuid
     * @return true if the registry was in a good state when this method is called (i.e. the first step had already
     * been undertaken for the instance being registered), false otherwise.
     */
    public boolean registerInstanceUuid(final int controlSocketHandle, final UUID instanceUuid) {
        if (!controlSocketHandleToInvokerSocketHandle.containsKey(controlSocketHandle)) {
            return false;
        }

        final int invokerSocketHandle = controlSocketHandleToInvokerSocketHandle.get(controlSocketHandle);

        instanceUuidToInvokerSocketHandle.put(instanceUuid, invokerSocketHandle);
        instanceUuidToControlSocketHandle.put(instanceUuid, controlSocketHandle);

        return true;
    }

    /**
     * Unregistering an instance is a single-step process: simply provide the Instance UUID, and the registry will
     * update all its underlying data structures.
     *
     * @param instanceUuid
     * @return true if the unregistration procedure succeeded (i.e. the data structures were in a good state to complete
     * the unregistration), false otherwise.
     */
    public boolean unregisterInstance(final UUID instanceUuid) {
        if (!instanceUuidToInvokerSocketHandle.containsKey(instanceUuid) ||
            !instanceUuidToControlSocketHandle.containsKey(instanceUuid)) {
            return false;
        }

        final int invokerSocketHandle = instanceUuidToInvokerSocketHandle.remove(instanceUuid);
        final int controlSocketHandle = instanceUuidToControlSocketHandle.remove(instanceUuid);

        invokerTcpAddressToSocketHandle.inverse().remove(invokerSocketHandle);
        controlTcpAddressToSocketHandle.inverse().remove(controlSocketHandle);

        controlSocketHandleToInvokerSocketHandle.remove(controlSocketHandle);

        final Set<NodeId> nodeIds = instanceUuidToNodeIds.remove(instanceUuid);

        if (nodeIds != null) {
            nodeIdToSocketHandle.keySet().removeAll(nodeIds);
        }

        instanceUuidToNodeSocketHandles.remove(instanceUuid);

        return true;
    }

    public void registerNode(final NodeId nodeId, final int nodeSocketHandle) {
        nodeIdToSocketHandle.put(nodeId, nodeSocketHandle);

        final UUID instanceUuid = nodeId.getInstanceUuid();

        final Set<NodeId> nodeIds = getOrCreateNodeIdSetForInstanceUuid(instanceUuid);
        nodeIds.add(nodeId);

        final Set<Integer> nodeSocketHandles = getOrCreateNodeSocketHandleSetForInstanceUuid(instanceUuid);
        nodeSocketHandles.add(nodeSocketHandle);
    }

    public void unregisterNode(final NodeId nodeId) {
        final int nodeSocketHandle = nodeIdToSocketHandle.remove(nodeId);

        final UUID instanceUuid = nodeId.getInstanceUuid();

        final Set<NodeId> nodeIds = getOrCreateNodeIdSetForInstanceUuid(instanceUuid);
        nodeIds.remove(nodeId);

        final Set<Integer> nodeSocketHandles = getOrCreateNodeSocketHandleSetForInstanceUuid(instanceUuid);
        nodeSocketHandles.remove(nodeSocketHandle);
    }

    public void registerBoundInvokerSocket(final String boundInvokerTcpAddress, final int boundInvokerSocketHandle) {
        this.boundInvokerTcpAddress = boundInvokerTcpAddress;
        this.boundInvokerSocketHandle = boundInvokerSocketHandle;
    }

    public void unregisterBoundInvokerSocket() {
        this.boundInvokerTcpAddress = null;
        this.boundInvokerSocketHandle = SOCKET_HANDLE_NOT_FOUND;
    }

    @Override
    public void close() {
        invokerTcpAddressToSocketHandle.clear();
        controlTcpAddressToSocketHandle.clear();

        controlSocketHandleToInvokerSocketHandle.clear();

        instanceUuidToInvokerSocketHandle.clear();
        instanceUuidToControlSocketHandle.clear();

        nodeIdToSocketHandle.clear();

        instanceUuidToNodeIds.clear();

        instanceUuidToNodeSocketHandles.clear();
    }
}