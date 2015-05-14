package com.namazustudios.socialengine.fts;

import com.namazustudios.socialengine.fts.annotation.SearchableIdentity;
import org.apache.lucene.document.Document;

/**
 * Represents a Key in index for a particular document.
 *
 * Created by patricktwohig on 5/13/15.
 */
public class GeneratorDocumentEntry implements DocumentEntry<Object, Object> {

    private final Document document;

    private Class<?> documentClass;
    private SearchableIdentity searchableIdentity;

    public GeneratorDocumentEntry() {
        this(new Document());
    }

    public GeneratorDocumentEntry(final Document document) {
        this.document = document;
    }

    @Override
    public String getClassName() {
        return document.get(SearchableIdentity.CLASS_FIELD_NAME);
    }

    @Override
    public Class<?> getDocumentClass() {

        if (documentClass == null) {
            documentClass = extractDocumentClass();
        }

        return documentClass;

    }

    private Class<?> extractDocumentClass() {

        final String className = getClassName();

        if (className == null) {
            return null;
        }

        try {
            return getClass().forName(className);
        } catch (ClassNotFoundException ex) {
            throw new DocumentGeneratorException(ex);
        }

    }

    @Override
    public Document getDocument() {
        return document;
    }

}
