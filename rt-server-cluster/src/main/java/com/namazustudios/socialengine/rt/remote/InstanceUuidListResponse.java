package com.namazustudios.socialengine.rt.remote;

import javolution.io.Struct;

import java.util.UUID;

public class InstanceUuidListResponse extends Struct {
    public static InstanceUuidListResponse buildInstanceUuidListResponse(final UUID instanceUuid) {
        final InstanceUuidListResponse instanceUuidListResponse = new InstanceUuidListResponse();
        instanceUuidListResponse.instanceUuid.set(instanceUuid);

        return instanceUuidListResponse;
    }

    public static InstanceUuidListResponse InstanceUuidListResponseFromBytes(final byte[] bytes) {
        final InstanceUuidListResponse instanceUuidListResponse = new InstanceUuidListResponse();
        instanceUuidListResponse.getByteBuffer().put(bytes);
        return instanceUuidListResponse;
    }

    public final PackedUUID instanceUuid = inner(new PackedUUID());
}
