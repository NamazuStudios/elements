package com.namazustudios.socialengine.fts;

import com.namazustudios.socialengine.fts.annotation.SearchableDocument;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.lucene.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * The default instance of the {@link DocumentGenerator}, this is a simple
 * implementation that just reads the annotations and converts to a document
 * using the "Default" setup.
 *
 * Created by patricktwohig on 5/12/15.
 */
public class DefaultDocumentGenerator extends AbstractDocumentGenerator {

    public DefaultDocumentGenerator() {
        super(DefaultIndexableFieldProcessorProvider.getInstance(),
             DefaultIndexableFieldExtractorProvider.getInstance());
    }

}
