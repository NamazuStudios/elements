package com.namazustudios.socialengine;

import com.namazustudios.socialengine.annotation.SearchableDocument;
import org.apache.lucene.document.Document;

/**
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
