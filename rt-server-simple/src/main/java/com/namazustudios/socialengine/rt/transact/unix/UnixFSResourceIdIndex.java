package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.transact.ResourceIdIndex;
import com.namazustudios.socialengine.rt.transact.Revision;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;

public class UnixFSResourceIdIndex implements ResourceIdIndex {

    @Override
    public Revision<Boolean> existsAt(final Revision<?> revision,
                                      final ResourceId resourceId) {
        return null;
    }

    @Override
    public ReadableByteChannel loadResourceContentsAt(final Revision<ReadableByteChannel> comparableTo,
                                                      final Path path) throws IOException {
        return null;
    }

    @Override
    public ReadableByteChannel loadResourceContentsAt(final Revision<ReadableByteChannel> comparableTo,
                                                      final ResourceId resourceId) throws IOException {
        return null;
    }

}
