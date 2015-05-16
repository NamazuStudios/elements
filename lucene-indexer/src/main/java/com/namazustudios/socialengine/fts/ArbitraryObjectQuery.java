package com.namazustudios.socialengine.fts;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

/**
 * Created by patricktwohig on 5/15/15.
 */
public class ArbitraryObjectQuery<DocumentT> extends ObjectQuery<DocumentT> {

    private final Query query;

    public ArbitraryObjectQuery(Class<DocumentT> documentType, final Query query) {
        super(documentType);

        final BooleanQuery booleanQuery = new BooleanQuery();
        booleanQuery.add(getTypeQuery(), BooleanClause.Occur.FILTER);
        booleanQuery.add(getTypeQuery(), BooleanClause.Occur.FILTER);
        this.query = booleanQuery;

    }

    @Override
    public Query getQuery() {
        return null;
    }

}
