package com.namazustudios.socialengine.rt.transact;

import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.ResourceService;
import com.namazustudios.socialengine.rt.id.ResourceId;

import java.util.SortedSet;
import java.util.stream.Stream;

public interface PathIndex {

    RevisionMap<Path, ResourceId> getRevisionMap();

    default Revision<ResourceId> getValueAt(final Revision<ResourceId> revision, final Path path) {
        return getRevisionMap().getValueAt(revision, path);
    }

    Revision<Stream<ResourceService.Listing>> list(Revision<Object> comparableTo, Path path);

}
