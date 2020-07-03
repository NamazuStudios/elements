package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.transact.Revision;
import com.namazustudios.socialengine.rt.transact.TransactionJournal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

class UnixFSJournalEntry implements TransactionJournal.Entry {

    private static final Logger logger = LoggerFactory.getLogger(UnixFSJournalEntry.class);

    private final NodeId nodeId;

    protected boolean open = true;

    protected final UnixFSUtils.IOOperationV onClose;

    public UnixFSJournalEntry(final NodeId nodeId,
                              final UnixFSUtils.IOOperationV onClose) {
        this.nodeId = nodeId;
        this.onClose = onClose;
    }

    protected void check() {
        if (!open) throw new IllegalStateException();
    }

    public NodeId getNodeId() {
        check();
        return nodeId;
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

}
