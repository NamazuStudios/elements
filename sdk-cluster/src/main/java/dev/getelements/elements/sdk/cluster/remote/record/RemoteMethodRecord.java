package dev.getelements.elements.sdk.cluster.remote.record;

import dev.getelements.elements.sdk.cluster.address.RemoteElementMethodAddress;
import dev.getelements.elements.sdk.cluster.remote.annotation.RemotelyInvokable;

/**
 * The remote address of a single {@link RemotelyInvokable} method.
 *
 * @param address the address of the method
 */
public record RemoteMethodRecord(RemoteElementMethodAddress address) {}
