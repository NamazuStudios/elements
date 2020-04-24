package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.transact.Revision;
import com.namazustudios.socialengine.rt.transact.RevisionMap;

import java.nio.file.Path;
import java.util.SortedSet;

public class UnixFSPathRevisionMap implements RevisionMap<Path, ResourceId> {

    @Override
    public Revision<ResourceId> getValueAt(Revision<ResourceId> revision, Path key) {
        return null;
    }

}
