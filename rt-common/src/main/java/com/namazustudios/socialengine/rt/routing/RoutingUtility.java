package com.namazustudios.socialengine.rt.routing;

import com.namazustudios.socialengine.rt.exception.RoutingException;
import com.namazustudios.socialengine.rt.id.HasNodeId;
import com.namazustudios.socialengine.rt.id.NodeId;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

public interface RoutingUtility {

    /**
     * Reduces the {@link List<Object>} address to a {@link Set<NodeId>}
     *
     * @param address
     * @return a {@link Set<NodeId>}
     */
    static Set<NodeId> reduceAddressToNodeIds(final List<Object> address) {
        return address.stream()
            .filter(o -> o != null)
            .map(RoutingUtility::checkAddressComponentHasNodeId)
            .map(h -> h.getNodeId())
            .collect(Collectors.toSet());
    }

    /**
     * Ensures that the following {@link List<Object>} can be redued to a single {@link NodeId}, throwing the
     * appropriate exception in the event that there is a mismatch among any of the {@link NodeId} instances returned.
     *
     * This relies on each component implementing {@link HasNodeId}.
     *
     * @param address the {@link List<Object>} making up the address
     * @return the single {@link NodeId}, throwing an exception if it could not be determined.
     */
    static NodeId reduceAddressToSingleNodeId(final List<Object> address) {
        return address.stream()
            .filter(Objects::nonNull)
            .map(RoutingUtility::checkAddressComponentHasNodeId)
            .map(HasNodeId::getNodeId)
            .filter(Objects::nonNull)
            .reduce((nid0, nid1) -> ensureDistinctNode(address, nid0, nid1))
            .orElseThrow(() -> new RoutingException("Could not determine NodeID from address: " + address));
    }

    /**
     * Converts the address, as represented by a {@link List<Object>}, to a human-readible string using the components'
     * {@link Object#toString()} method.
     *
     * @param address the address
     * @return the string representation
     */
    static String addressToString(final List<Object> address) {
        return "[" + address.stream().map(o -> o.toString()).collect(joining(",")) + "]";
    }

    /**
     * Ensure that the component is an instance of {@link HasNodeId} throwing a {@link RoutingException} if the object
     * is not an instance of {@link HasNodeId}.
     *
     * @param object the object to check
     * @return the object cast as {@link HasNodeId}
     */
    static HasNodeId checkAddressComponentHasNodeId(final Object object) {
        try {
            return (HasNodeId) object;
        } catch (ClassCastException ex) {
            final String msg = "Address component " + object + " is not HasNodeId";
            throw new RoutingException(msg, ex);
        }
    }

    /**
     * A reducer function intended to ensure that the supplied {@link NodeId}s are all the same.
     *
     * @param address the original address
     * @param nodeId0 the first {@link NodeId}
     * @param nodeId1 the second {@link NodeId}
     * @return the first {@link NodeId}
     */
    static NodeId ensureDistinctNode(final List<Object> address, final NodeId nodeId0, final NodeId nodeId1) {

        if (!nodeId0.equals(nodeId1)) {
            final String msg = "Indistinct address.  All NodeIds must match " + addressToString(address);
            throw new RoutingException(msg);
        }

        return nodeId1;

    }

}
