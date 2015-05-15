package com.namazustudios.socialengine.fts;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;

/**
 * Thrown by {@link IndexableFieldExtractor} to indicate that it was unable to extract
 * a value from an instance of {@link IndexableField}.  This keeps an instance of
 * the {@link FieldMetadata} and the {@link Document} such that the caller may make
 * sense of the data and correct it.
 *
 * Created by patricktwohig on 5/15/15.
 */
public class FieldExtractionException extends DocumentGeneratorException {

    private final Document document;
    private final FieldMetadata fieldMetadata;

    public FieldExtractionException(final FieldMetadata fieldMetadata, final Document document) {
        this.fieldMetadata = fieldMetadata;
        this.document = document;
    }

    public FieldExtractionException(final FieldMetadata fieldMetadata, final Document document, String message) {
        super(message);
        this.fieldMetadata = fieldMetadata;
        this.document = document;
    }

    public FieldExtractionException(final FieldMetadata fieldMetadata, final Document document, String message, Throwable cause) {
        super(message, cause);
        this.fieldMetadata = fieldMetadata;
        this.document = document;
    }

    public FieldExtractionException(final FieldMetadata fieldMetadata, final Document document, Throwable cause) {
        super(cause);
        this.fieldMetadata = fieldMetadata;
        this.document = document;
    }

    public FieldExtractionException(final FieldMetadata fieldMetadata, final Document document, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.fieldMetadata = fieldMetadata;
        this.document = document;
    }

    public Document getDocument() {
        return document;
    }

    public FieldMetadata getFieldMetadata() {
        return fieldMetadata;
    }

}
