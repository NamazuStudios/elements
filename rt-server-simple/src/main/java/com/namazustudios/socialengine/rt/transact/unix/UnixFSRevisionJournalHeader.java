package com.namazustudios.socialengine.rt.transact.unix;

import javolution.io.Struct;

final class UnixFSRevisionJournalHeader extends Struct {

    static final int SIZE = new UnixFSRevisionJournalHeader().size();

    final UTF8String magic = new UTF8String(4);

    final Unsigned32 crc32 = new Unsigned32();

    final Unsigned32 snapshot = new Unsigned32();

    final Unsigned32 txnCount = new Unsigned32();

    final Enum8<UnixFSChecksumAlgorithm> algorithm = new Enum8<>(UnixFSChecksumAlgorithm.values());

}
