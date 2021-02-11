package com.namazustudios.socialengine.exception.cdnserve;

import java.io.IOException;

public class SymbolicLinkIOException extends IOException {

    public SymbolicLinkIOException() {}

    public SymbolicLinkIOException(String message) {
        super(message);
    }

    public SymbolicLinkIOException(String message, Throwable cause) {
        super(message, cause);
    }

    public SymbolicLinkIOException(Throwable cause) {
        super(cause);
    }
}
