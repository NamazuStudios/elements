package dev.getelements.elements.rt.transact.unix;

import javolution.io.Struct;

/**
 * Header type for the {@link UnixFSTransactionJournal}.
 */
class UnixFSJournalHeader extends Struct {

    /**
     * The magic bits of hte file.
     */
    final UTF8String magic = new UTF8String(4);

    /**
     * The major version.
     */
    final Signed32 major = new Signed32();

    /**
     * The minor version.
     */
    final Signed32 minor = new Signed32();

    /**
     * The counter governing the entire state of the transaction journal.
     */
    final UnixFSAtomicLongData counter = inner(new UnixFSAtomicLongData());

}
