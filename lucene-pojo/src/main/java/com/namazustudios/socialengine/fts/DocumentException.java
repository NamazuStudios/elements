package com.namazustudios.socialengine.fts;

import org.apache.lucene.document.Document;

/**
 * A base exception class used to relay any problems generating {@link Document} instances
 * from POJOs.
 *
 * Created by patricktwohig on 5/13/15.
 */
public class DocumentException extends RuntimeException {

    public DocumentException() {}

    public DocumentException(String message) {
        super(message);
    }

    public DocumentException(String message, Throwable cause) {
        super(message, cause);
    }

    public DocumentException(Throwable cause) {
        super(cause);
    }

    public DocumentException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
