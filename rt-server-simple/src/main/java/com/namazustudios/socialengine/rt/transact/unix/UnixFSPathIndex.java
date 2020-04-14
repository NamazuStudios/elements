package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.ResourceService;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.transact.PathIndex;
import com.namazustudios.socialengine.rt.transact.Revision;
import com.namazustudios.socialengine.rt.transact.RevisionMap;

import java.util.stream.Stream;

public class UnixFSPathIndex implements PathIndex {

    @Override
    public RevisionMap<Path, ResourceId> getRevisionMap() {
        return null;
    }

    @Override
    public Revision<Stream<ResourceService.Listing>> list(final Revision<?> revision,
                                                          final Path path) {
        return null;
    }

}
