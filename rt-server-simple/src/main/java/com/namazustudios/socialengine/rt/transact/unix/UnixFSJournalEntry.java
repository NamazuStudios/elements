package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.transact.Revision;
import com.namazustudios.socialengine.rt.transact.TransactionJournal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

class UnixFSJournalEntry implements TransactionJournal.Entry {

    private static final Logger logger = LoggerFactory.getLogger(UnixFSJournalEntry.class);

    protected boolean open = true;

    protected final UnixFSUtils.IOOperationV onClose;

    protected UnixFSTransactionProgram program;

    public UnixFSJournalEntry(final UnixFSUtils.IOOperationV onClose) {
        this.onClose = onClose;
    }

    protected void check() {
        if (!open) throw new IllegalStateException();
    }

    @Override
    public void close() {
        if (open) {
            try {
                open = false;
                onClose.perform();
            } catch (IOException ex) {
                logger.error("Caught IOException closing entry.", ex);
            }
        }
    }

    @Override
    public <EntryT extends TransactionJournal.Entry> EntryT getOriginal(final Class<EntryT> originalType) {
        try {
            return originalType.cast(this);
        } catch (ClassCastException ex) {
            throw new IllegalArgumentException("Incompatible journal entry type.", ex);
        }
    }

    public UnixFSTransactionProgram getProgram() {
        if (program == null) throw new IllegalStateException("No program set.");
        return program;
    }

    public NodeId getNodeId() {
        if (program == null) throw new IllegalStateException("No program set.");
        return program.header.nodeId.get();
    }

}
