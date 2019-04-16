package com.namazustudios.socialengine.rt.jeromq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SocketHandleRegistry implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(SocketHandleRegistry.class);

    private final Map<Integer, String> tcpSocketHandlesToAddresses = new LinkedHashMap<>();
    private final Map<String, Integer> tcpAddressesToSocketHandles = new LinkedHashMap<>();

    private final Map<Integer, UUID> inprocSocketHandlesToIdentifiers = new LinkedHashMap<>();
    private final Map<UUID, Integer> inprocIdentifiersToSocketHandles = new LinkedHashMap<>();

    public boolean hasTcpAddress(final String tcpAddress) {
        return tcpAddressesToSocketHandles.containsKey(tcpAddress);
    }

    public boolean hasInprocIdentifier(final UUID inprocIdentifier) {
        return inprocIdentifiersToSocketHandles.containsKey(inprocIdentifier);
    }

    public void registerTcpSocketHandle(final int tcpSocketHandle, final String tcpAddress) {
        tcpSocketHandlesToAddresses.put(tcpSocketHandle, tcpAddress);
        tcpAddressesToSocketHandles.put(tcpAddress, tcpSocketHandle);
    }

    public void unregisterTcpSocketHandle(final int tcpSocketHandle) {
        final String tcpAddress = tcpSocketHandlesToAddresses.get(tcpSocketHandle);

        tcpSocketHandlesToAddresses.remove(tcpSocketHandle);
        tcpAddressesToSocketHandles.remove(tcpAddress);
    }

    public void registerInprocSocketHandle(final int inprocSocketHandle, final UUID inprocIdentifier) {
        inprocSocketHandlesToIdentifiers.put(inprocSocketHandle, inprocIdentifier);
        inprocIdentifiersToSocketHandles.put(inprocIdentifier, inprocSocketHandle);
    }

    public void unregisterInprocSocketHandle(final int inprocSocketHandle) {
        final UUID inprocIdentifier = inprocSocketHandlesToIdentifiers.get(inprocSocketHandle);

        inprocSocketHandlesToIdentifiers.remove(inprocSocketHandle);
        inprocIdentifiersToSocketHandles.remove(inprocIdentifier);
    }

    /**
     * Gets the tcp socket handle for the given full tcp address.
     *
     * @param tcpAddress
     * @return the socket handle if found, -1 otherwise.
     */
    public int getTcpSocketHandle(final String tcpAddress) {
        if (!tcpAddressesToSocketHandles.containsKey(tcpAddress)) {
            return -1;
        }

        final Integer tcpSocketHandle = tcpAddressesToSocketHandles.get(tcpAddress);

        return tcpSocketHandle;
    }

    /**
     * Gets the inproc socket handle for the given inproc identifier.
     *
     * @param inprocIdentifier
     * @return the socket handle if found, -1 otherwise.
     */
    public int getInprocSocketHandle(final UUID inprocIdentifier) {
        if (!inprocIdentifiersToSocketHandles.containsKey(inprocIdentifier)) {
            return -1;
        }

        final Integer inprocSocketHandle = inprocIdentifiersToSocketHandles.get(inprocIdentifier);

        return inprocSocketHandle;
    }

    @Override
    public void close() {
        tcpSocketHandlesToAddresses.clear();
        tcpAddressesToSocketHandles.clear();
        inprocSocketHandlesToIdentifiers.clear();
        inprocIdentifiersToSocketHandles.clear();
    }
}