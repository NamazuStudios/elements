package com.namazustudios.socialengine;

import com.namazustudios.socialengine.annotation.SearchableDocument;
import org.apache.lucene.document.Document;

/**
 * The default instance of the {@link DocumentGenerator}, this is a simple
 * implementation that just reads the annotations and converts.
 *
 * Created by patricktwohig on 5/12/15.
 */
public class DefaultDocumentGenerator implements DocumentGenerator {

    @Override
    public void analyze(Class<?> cls) {
        // TODO Implement me
    }

    @Override
    public Document generate(Object object) {
        // TODO Implement me
        return null;
    }

}
