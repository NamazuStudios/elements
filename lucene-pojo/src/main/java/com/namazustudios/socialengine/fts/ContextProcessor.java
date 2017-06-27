package com.namazustudios.socialengine.fts;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;

/**
 * Used to process a particular {@link JXPathContext}, inserting necessary
 * {@link IndexableField} into the given document entry as needed.
 *
 * Created by patricktwohig on 5/13/15.
 */
@FunctionalInterface
public interface ContextProcessor {

    /**
     * Given an instance of {@link JXPathContext} and a {@link Document} this
     * processes the given document for the given context.
     *
     *  @param context
     * @param documentEntry
     */
    void process(JXPathContext context, DocumentEntry<?> documentEntry);

}
