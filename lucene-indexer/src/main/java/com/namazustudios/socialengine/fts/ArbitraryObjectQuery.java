package com.namazustudios.socialengine.fts;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

/**
 * An {@link ObjectQuery} which can accept an arbitrary query.
 *
 * Created by patricktwohig on 5/15/15.
 */
public class ArbitraryObjectQuery<DocumentT> extends ObjectQuery<DocumentT> {

    private final Query arbitraryQuery;

    public ArbitraryObjectQuery(final Class<DocumentT> documentType,
                                final IndexableFieldProcessor.Provider indexableFieldProcessorProvider,
                                final Query arbitraryQuery) {
        super(documentType, indexableFieldProcessorProvider);
        this.arbitraryQuery = arbitraryQuery;
    }

    @Override
    public Query getQuery() {
        final BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
        booleanQueryBuilder.add(getTypeQuery(), BooleanClause.Occur.FILTER);
        booleanQueryBuilder.add(arbitraryQuery, BooleanClause.Occur.FILTER);
        return booleanQueryBuilder.build();
    }

}
