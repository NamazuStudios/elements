package com.namazustudios.socialengine.fts;

import org.apache.lucene.document.Document;

/**
 * Created by patricktwohig on 5/15/15.
 */
public class DocumentGenerationException extends DocumentException {

    private final Document document;

    private final Object value;

    private final FieldMetadata field;

    public DocumentGenerationException(final Document document, final Object value, final FieldMetadata field) {
        this.document = document;
        this.value = value;
        this.field = field;
    }

    public DocumentGenerationException(final Document document, final Object value, final FieldMetadata field, String message) {
        super(message);
        this.document = document;
        this.value = value;
        this.field = field;
    }

    public DocumentGenerationException(final Document document, final Object value, final FieldMetadata field, String message, Throwable cause) {
        super(message, cause);
        this.document = document;
        this.value = value;
        this.field = field;
    }

    public DocumentGenerationException(final Document document, final Object value, final FieldMetadata field, Throwable cause) {
        super(cause);
        this.document = document;
        this.value = value;
        this.field = field;
    }

    public DocumentGenerationException(final Document document, final Object value, final FieldMetadata field, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.document = document;
        this.value = value;
        this.field = field;
    }
}
