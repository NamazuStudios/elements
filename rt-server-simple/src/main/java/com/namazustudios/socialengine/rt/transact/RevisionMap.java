package com.namazustudios.socialengine.rt.transact;

import java.util.Map;
import java.util.SortedSet;

public interface RevisionMap<KeyT, ValueT> {

    SortedSet<Revision<ValueT>>  getRevisions(KeyT key);

    default Revision<ValueT> getValueAt(final Revision<ValueT> revision, final KeyT key) {
        return getRevisions(key).tailSet(revision).first();
    }

}
