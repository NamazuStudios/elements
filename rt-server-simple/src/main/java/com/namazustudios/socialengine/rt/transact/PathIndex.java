package com.namazustudios.socialengine.rt.transact;

import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.ResourceService;
import com.namazustudios.socialengine.rt.id.ResourceId;

import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Stream;

public interface PathIndex {

    RevisionMap<Path, ResourceId> getRevisionMap();

    RevisionMap<ResourceId, Set<Path>> getReverseRevisionMap();

    Revision<Stream<ResourceService.Listing>> list(Revision<?> revision, Path path);

}
