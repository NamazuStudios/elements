package com.namazustudios.socialengine.rt.transact;

import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.id.ResourceId;

import java.util.Set;
import java.util.SortedSet;

public interface ReversePathIndex {

    RevisionMap<ResourceId, Set<Path>> getRevisionMap();

    default Revision<Set<Path>> getValueAt(final ResourceId resourceId, final Revision<Set<Path>> revision) {
        return getRevisionMap().getValueAt(revision, resourceId);
    }

}
