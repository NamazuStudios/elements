package com.namazustudios.socialengine.rt.remote;

import java.util.UUID;

public interface MultiplexSupport {

    /**
     * Makes a type 3 named {@link UUID} from the supplied node id using the appropriate charset encoding.  The returned
     * {@link UUID} can then be used to assign inprocIdentifier routes using {@link #issueOpenInprocChannelCommand(String,UUID)}.
     *
     * @param nodeIdentifier the node id
     * @return the {@link UUID} of the inprocIdentifier
     */
    UUID getInprocIdentifierForNodeIdentifier(String nodeIdentifier);

    /**
     * Adds a {@link UUID} for a particular inprocIdentifier.
     *
     * @param inprocIdentifier the {@link UUID} inprocIdentifier
     */
    void issueOpenInprocChannelCommand(final String backendAddress, UUID inprocIdentifier);

    /**
     * Removes {@link UUID} for a particular inprocIdentifier.
     *
     * @param inprocIdentifier the {@link UUID} inprocIdentifier
     */
    void issueCloseInprocChannelCommand(final String backendAddress, UUID inprocIdentifier);

    /**
     * Opens a route to the supplied inprocIdentifier node ID.
     *
     * @param nodeIdentifier the inproc's node id
     */
    default void issueOpenInprocChannelCommand(final String backendAddress, final String nodeIdentifier) {
        final UUID inprocIdentifier = getInprocIdentifierForNodeIdentifier(nodeIdentifier);
        issueOpenInprocChannelCommand(backendAddress, inprocIdentifier);
    }

    /**
     * Closes a route to the supplied inprocIdentifier node ID.
     *
     * @param nodeIdentifier the inproc's node id
     */
    default void issueCloseInprocChannelCommand(final String backendAddress, final String nodeIdentifier) {
        final UUID inprocIdentifier = getInprocIdentifierForNodeIdentifier(nodeIdentifier);
        issueCloseInprocChannelCommand(backendAddress, inprocIdentifier);
    }

    void issueOpenBackendChannelCommand(final String backendAddress);

    void issueCloseBackendChannelCommand(final String backendAddress);

}
