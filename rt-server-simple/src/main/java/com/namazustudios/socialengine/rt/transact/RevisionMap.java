package com.namazustudios.socialengine.rt.transact;

import java.util.Map;
import java.util.SortedSet;

public interface RevisionMap<KeyT, ValueT> {

    Revision<ValueT> getValueAt(final Revision<?> revision, final KeyT key);

}
