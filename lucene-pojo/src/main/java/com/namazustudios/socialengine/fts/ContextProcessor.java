package com.namazustudios.socialengine.fts;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.lucene.document.Document;

/**
 * Created by patricktwohig on 5/13/15.
 */
public interface ContextProcessor {

    /**
     * Given an instance of {@link JXPathContext} and a {@link Document} this
     * processes the given document for hte given context.
     *  @param context
     * @param documentEntry
     */
    void process(JXPathContext context, DocumentEntry<?,?> documentEntry);

}
