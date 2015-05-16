package com.namazustudios.socialengine.fts;

import com.namazustudios.socialengine.fts.annotation.SearchableDocument;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

/**
 * Wrapper around a Lucene {@link Query} for a specific type in the search index.  This class
 * can be used as the base query for a type, and additionally this can be used to find
 * a specific object in the index.
 *
 * The specific-object query is used internally in managing the index.
 *
 * Created by patricktwohig on 5/15/15.
 */
public abstract class ObjectQuery<DocumentT> {

    private final Query query;

    private final Class<DocumentT> documentType;

    public ObjectQuery(final Class<DocumentT> documentType) {

        final SearchableDocument searchableDocument = documentType.getAnnotation(SearchableDocument.class);

        if (searchableDocument == null) {
            throw new IllegalArgumentException( documentType + " does not have annotation " + SearchableDocument.class);
        }

        final Term term = new Term(searchableDocument.type().name(), documentType.getName());

        this.query = new TermQuery(term);
        this.documentType = documentType;

    }

    /**
     * Gets the type returned by this query.
     *
     * @return the type returned by this query
     */
    public Class<DocumentT> getDocumentType() {
        return documentType;
    }

    /**
     * The raw Query object that will find all objects of the
     * particuular type.
     *
     * @return the raw query, untyped.
     */
    public Query getTypeQuery() {
        return query;
    }

    /**
     * Gets the actual Query to run.
     *
     * @return the actual Query to run
     */
    public abstract Query getQuery();

}
