package com.namazustudios.socialengine.rt;

import java.util.UUID;

/**
 * Signifies that an object adhering to this interface provides information to address Remote invocations to a
 * particular Instance in the deployment.
 */
public interface NetworkAddressor {
    UUID getInstanceUuid();
}
