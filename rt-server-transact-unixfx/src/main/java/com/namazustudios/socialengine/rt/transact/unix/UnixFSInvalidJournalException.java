package com.namazustudios.socialengine.rt.transact.unix;

import java.io.IOException;

public class UnixFSInvalidJournalException extends IOException {

    public UnixFSInvalidJournalException() {}

    public UnixFSInvalidJournalException(String s) {
        super(s);
    }

    public UnixFSInvalidJournalException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public UnixFSInvalidJournalException(Throwable throwable) {
        super(throwable);
    }

}

