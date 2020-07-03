package com.namazustudios.socialengine.rt.transact.unix;

import javolution.io.Struct;

/**
 * Header type for the {@link UnixFSTransactionJournal}.
 */
class UnixFSJournalHeader extends Struct {

    final UTF8String magic = new UTF8String(4);

    final Signed32 major = new Signed32();

    final Signed32 minor = new Signed32();

    final Unsigned32 txnBufferSize = new Unsigned32();

    final Unsigned32 txnBufferCount = new Unsigned32();

}
