package com.namazustudios.socialengine.rt.remote;

import javolution.io.Struct;

import java.util.UUID;

public class StatusResponse extends Struct {
    public static StatusResponse buildStatusResponse(
            UUID nodeIdentifier,
            double loadAverage,
            long inMemoryResourceCount
    ) {
        if (nodeIdentifier == null) {
            throw new IllegalArgumentException("nodeIdentifier must not be null.");
        }

        final StatusResponse statusResponse = new StatusResponse();

        statusResponse.nodeIdentifier.set(nodeIdentifier);
        statusResponse.loadAverage.set(loadAverage);
        statusResponse.inMemoryResourceCount.set(inMemoryResourceCount);

        return statusResponse;
    }

    public final PackedUUID nodeIdentifier = inner(new PackedUUID());
    public final Float64 loadAverage = new Float64();
    public final Signed64 inMemoryResourceCount = new Signed64();

}
