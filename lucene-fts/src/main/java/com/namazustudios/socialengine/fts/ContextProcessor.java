package com.namazustudios.socialengine.fts;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.lucene.document.Document;

/**
 * Created by patricktwohig on 5/13/15.
 */
interface ContextProcessor {

    void process(JXPathContext context, Document document);

}
