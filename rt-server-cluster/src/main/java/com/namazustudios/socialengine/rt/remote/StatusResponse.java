package com.namazustudios.socialengine.rt.remote;

import javolution.io.Struct;

import java.util.UUID;

public class StatusResponse extends Struct {
    public static StatusResponse buildStatusResponse(UUID instanceUuid) {
        if (instanceUuid == null) {
            throw new IllegalArgumentException("InstanceUuid must not be null.");
        }

        final StatusResponse statusResponse = new StatusResponse();

        statusResponse.instanceUuid.set(instanceUuid);

        return statusResponse;
    }

    public final PackedUUID instanceUuid = inner(new PackedUUID());
}
