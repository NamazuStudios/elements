package com.namazustudios.socialengine.fts;

import org.apache.lucene.search.Query;

/**
 * Created by patricktwohig on 5/15/15.
 */
public class WildcardObjectQuery<DocumentT> extends ObjectQuery<DocumentT> {

    public WildcardObjectQuery(Class<DocumentT> documentType,
                               IndexableFieldProcessor.Provider indexableFieldProcessorProvider) {
        super(documentType, indexableFieldProcessorProvider);
    }

    @Override
    public Query getQuery() {
        return getTypeQuery();
    }

}
