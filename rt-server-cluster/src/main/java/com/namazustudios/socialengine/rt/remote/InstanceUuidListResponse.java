package com.namazustudios.socialengine.rt.remote;

import javolution.io.Struct;

import java.util.UUID;

/**
 * A list of Instance UUIDs, one for each Instance to which we are fully connected, i.e. for each Instance, the current
 * process meets the criteria:
 *
 * 1) We have established an invoker connection
 * 2) We have established a control connection
 * 3) We have retrieved its Instance UUID and correlated it to our invoker/control sockets/connections
 *
 * Note that we provide no guarantee on the ordering of Instance UUIDs--even though they are sent serially, they should
 * be thought of as a Set (i.e. no dupes, no ordering).
 */
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
