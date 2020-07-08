package com.namazustudios.socialengine.rt.transact.unix;

import javolution.io.Struct;

class UnixFSRevisionDataStoreRevision extends Struct {

    static int SIZE = new UnixFSRevisionDataStoreRevision().size();

    final Enum8<UnixFSChecksumAlgorithm> algorithm = new Enum8<>(UnixFSChecksumAlgorithm.values());

    final Unsigned32 checksum = new Unsigned32();

    final UnixFSRevisionData revision = inner(new UnixFSRevisionData());

}
