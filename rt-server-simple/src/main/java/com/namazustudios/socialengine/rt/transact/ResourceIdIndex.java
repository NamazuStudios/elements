package com.namazustudios.socialengine.rt.transact;

import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.id.ResourceId;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;

public interface ResourceIdIndex {

    Revision<Boolean> existsAt(Revision<?> revision, ResourceId resourceId);

    ReadableByteChannel loadResourceContentsAt(Revision<ReadableByteChannel> comparableTo, Path path) throws IOException;

    ReadableByteChannel loadResourceContentsAt(Revision<ReadableByteChannel> comparableTo, ResourceId resourceId) throws IOException;

}
