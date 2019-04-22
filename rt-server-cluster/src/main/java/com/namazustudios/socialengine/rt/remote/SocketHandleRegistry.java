package com.namazustudios.socialengine.rt.remote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Maps socket handles to tcp addresses/inproc identifiers (and vice versa). Note: this is not a thread-safe module and
 * is meant to be accessed only within a connection runnable thread.
 */
public class SocketHandleRegistry implements AutoCloseable {

    public static final String LOCAL_TCP_ADDRESS_REPRESENTATION = "";

    public static final int SOCKET_HANDLE_NOT_FOUND = -1;

    private static final Logger logger = LoggerFactory.getLogger(SocketHandleRegistry.class);

    private final Map<Integer, String> tcpSocketHandlesToAddresses = new LinkedHashMap<>();
    private final Map<String, Integer> tcpAddressesToSocketHandles = new LinkedHashMap<>();

    private final Map<Integer, UUID> inprocSocketHandlesToIdentifiers = new LinkedHashMap<>();
    private final Map<UUID, Integer> inprocIdentifiersToSocketHandles = new LinkedHashMap<>();

    private final Map<UUID, String> inprocIdentifiersToTcpAddresses = new LinkedHashMap<>();
    private final Map<String, Set<UUID>> tcpAddressesToInprocIdentifiers = new LinkedHashMap<>();

    private Set<UUID> getOrCreateInprocIdentifierSet(final String tcpAddress) {
        final Set<UUID> inprocIdentifierSet;
        if (tcpAddressesToInprocIdentifiers.containsKey(tcpAddress)) {
            inprocIdentifierSet = tcpAddressesToInprocIdentifiers.get(tcpAddress);
        }
        else {
            inprocIdentifierSet = new LinkedHashSet<>();
            tcpAddressesToInprocIdentifiers.put(tcpAddress, inprocIdentifierSet);
        }

        return inprocIdentifierSet;
    }

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

    /**
     * Registers an
     *
     * @param inprocSocketHandle the socket handle for the inproc connection.
     * @param inprocIdentifier the UUID inproc identifier.
     * @param tcpAddress the tcp address where the inproc socket lives.
     */
    public void registerInprocSocketHandle(
            final int inprocSocketHandle,
            final String tcpAddress,
            final UUID inprocIdentifier
    ) {
        inprocSocketHandlesToIdentifiers.put(inprocSocketHandle, inprocIdentifier);
        inprocIdentifiersToSocketHandles.put(inprocIdentifier, inprocSocketHandle);


        inprocIdentifiersToTcpAddresses.put(inprocIdentifier, tcpAddress);
        final Set<UUID> inprocIdentifiers = getOrCreateInprocIdentifierSet(tcpAddress);

        inprocIdentifiers.add(inprocIdentifier);
    }

    public void unregisterInprocSocketHandle(final int inprocSocketHandle) {
        final UUID inprocIdentifier = inprocSocketHandlesToIdentifiers.get(inprocSocketHandle);

        inprocSocketHandlesToIdentifiers.remove(inprocSocketHandle);
        inprocIdentifiersToSocketHandles.remove(inprocIdentifier);
        inprocIdentifiersToTcpAddresses.remove(inprocIdentifier);
    }

    /**
     * Gets the tcp socket handle for the given full tcp address.
     *
     * @param tcpAddress
     * @return the socket handle if found, {@link SocketHandleRegistry#SOCKET_HANDLE_NOT_FOUND} otherwise.
     */
    public int getTcpSocketHandle(final String tcpAddress) {
        if (!tcpAddressesToSocketHandles.containsKey(tcpAddress)) {
            return SOCKET_HANDLE_NOT_FOUND;
        }

        final Integer tcpSocketHandle = tcpAddressesToSocketHandles.get(tcpAddress);

        return tcpSocketHandle;
    }

    /**
     * Gets the tcp address string associated with the given socket handle, if it exists.
     *
     * @param socketHandle
     * @return the {@link String} tcp address if it exists, null otherwise.
     */
    public String getTcpAddress(final int socketHandle) {
        if (!tcpSocketHandlesToAddresses.containsKey(socketHandle)) {
            return null;
        }

        final String tcpAddress = tcpSocketHandlesToAddresses.get(socketHandle);

        return tcpAddress;
    }

    /**
     * Gets the inproc socket handle for the given inproc identifier.
     *
     * @param inprocIdentifier
     * @return the socket handle if found, {@link SocketHandleRegistry#SOCKET_HANDLE_NOT_FOUND} otherwise.
     */
    public int getInprocSocketHandle(final UUID inprocIdentifier) {
        if (!inprocIdentifiersToSocketHandles.containsKey(inprocIdentifier)) {
            return SOCKET_HANDLE_NOT_FOUND;
        }

        final Integer inprocSocketHandle = inprocIdentifiersToSocketHandles.get(inprocIdentifier);

        return inprocSocketHandle;
    }

    /**
     * Gets the inproc identifier associated with the given socket handle, if it exists.
     *
     * @param socketHandle
     * @return the {@link UUID} inprocIdentifier if it exists, null otherwise.
     */
    public UUID getInprocIdentifier(final int socketHandle) {
        if (!inprocSocketHandlesToIdentifiers.containsKey(socketHandle)) {
            return null;
        }

        final UUID inprocIdentifier = inprocSocketHandlesToIdentifiers.get(socketHandle);

        return inprocIdentifier;
    }

    /**
     *
     * @param inprocIdentifier
     * @return the {@link String} tcp address if it exists, null otherwise.
     */
    public String getTcpAddressForInprocIdentifier(final UUID inprocIdentifier) {
        if (!inprocIdentifiersToTcpAddresses.containsKey(inprocIdentifier)) {
            return null;
        }

        return inprocIdentifiersToTcpAddresses.get(inprocIdentifier);
    }

    /**
     * Returns all inproc identifiers that have been registered for the given {@param tcpAddress}.
     *
     * @param tcpAddress
     * @return a {@link Set<UUID>} of inproc identifiers associated with the tcpAddress.
     */
    public Set<UUID> getInprocIdentifiersForTcpAddress(final String tcpAddress) {
        final Set<UUID> inprocIdentifiers = getOrCreateInprocIdentifierSet(tcpAddress);
        return inprocIdentifiers;
    }

    /**
     * Returns all inproc identifiers that have been registered for localhost tcp.
     *
     * @return a {@link Set<UUID>} of inproc identifiers associated with the local tcp address.
     */
    public Set<UUID> getLocalTcpInprocIdentifiers() {
        final Set<UUID> inprocIdentifiers = getOrCreateInprocIdentifierSet(LOCAL_TCP_ADDRESS_REPRESENTATION);
        return inprocIdentifiers;
    }



    @Override
    public void close() {
        tcpSocketHandlesToAddresses.clear();
        tcpAddressesToSocketHandles.clear();

        inprocSocketHandlesToIdentifiers.clear();
        inprocIdentifiersToSocketHandles.clear();

        inprocIdentifiersToTcpAddresses.clear();
        tcpAddressesToInprocIdentifiers.clear();
    }
}