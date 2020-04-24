package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.transact.Revision;

public class UnixFSRevision<RevisionT> implements Revision<RevisionT> {

    @Override
    public String getUniqueIdentifier() {
        return null;
    }

    @Override
    public int compareTo(Revision<?> o) {
        return 0;
    }

}
