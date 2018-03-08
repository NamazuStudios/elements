package com.namazustudios.socialengine.fts;

import com.namazustudios.socialengine.fts.annotation.SearchableField;
import com.namazustudios.socialengine.fts.annotation.SearchableIdentity;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

/**
 * Created by patricktwohig on 5/15/15.
 */
public class IdentityObjectQuery<DocumentT> extends ObjectQuery<DocumentT> {

    private final Object identifier;

    public IdentityObjectQuery(final Class<? extends DocumentT> documentType,
                               final IndexableFieldProcessor.Provider indexableFieldProcessorProvider,
                               Object identifier) {
        super(documentType, indexableFieldProcessorProvider);
        this.identifier = identifier;
    }

    @Override
    public Query getQuery() {

        Class<?> cls = getDocumentType();
        SearchableField searchableField = null;

        do {

            final SearchableIdentity searchableIdentity = cls.getAnnotation(SearchableIdentity.class);

            if (searchableIdentity != null) {
                searchableField = searchableIdentity.value();
                break;
            }

            cls = cls.getSuperclass();

        } while (cls != null);

        if (searchableField == null) {
            throw new DocumentException("cannot find " +
                                        SearchableIdentity.class +
                                        " anywhere in the type" +
                                        " heirarchy for " +
                                        getDocumentType());
        }

        final BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
        booleanQueryBuilder.add(getTypeQuery(), BooleanClause.Occur.FILTER);
        addTermsToQuery(booleanQueryBuilder, searchableField, identifier);
        return booleanQueryBuilder.build();

    }

}
