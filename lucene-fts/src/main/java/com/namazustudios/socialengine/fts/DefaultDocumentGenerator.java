package com.namazustudios.socialengine.fts;

import org.apache.lucene.document.Document;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The default instance of the {@link DocumentGenerator}, this is a simple
 * implementation that just reads the annotations and converts.
 *
 * Created by patricktwohig on 5/12/15.
 */
public class DefaultDocumentGenerator extends AbstractDocumentGenerator implements DocumentGenerator {

    @Override
    public void analyze(Class<?> cls) {
        // TODO Implement me
    }

    @Override
    public void process(Object object, Document document) {

    }

}
