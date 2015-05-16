package com.namazustudios.socialengine.fts;

import com.namazustudios.socialengine.fts.annotation.SearchableDocument;
import com.namazustudios.socialengine.fts.annotation.SearchableField;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
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

    private final Class<DocumentT> documentType;

    private final SearchableDocument searchableDocument;

    private final IndexableFieldProcessor.Provider indexableFieldProcessorProvider;

    public ObjectQuery(final Class<DocumentT> documentType,
                       IndexableFieldProcessor.Provider indexableFieldProcessorProvider) {

        final SearchableDocument searchableDocument = documentType.getAnnotation(SearchableDocument.class);

        if (searchableDocument == null) {
            throw new IllegalArgumentException( documentType + " does not have annotation " + SearchableDocument.class);
        }

        this.documentType = documentType;
        this.searchableDocument = searchableDocument;
        this.indexableFieldProcessorProvider = indexableFieldProcessorProvider;

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
     * @return the raw query which will fetch the objects by type.
     */
    public Query getTypeQuery() {
        final BooleanQuery booleanQuery = new BooleanQuery();
        final SearchableField searchableField = searchableDocument.type();
        addTermsToQuery(booleanQuery, searchableField, getDocumentType());
        return booleanQuery;
    }

    /**
     * Gets the actual Query to run.
     *
     * @return the actual Query to run
     */
    public abstract Query getQuery();

    protected void addTermsToQuery(final BooleanQuery booleanQuery,
                                 final SearchableField searchableField,
                                 final Object value) {

        final FieldMetadata fieldMetadata = new AnnotationFieldMetadata(searchableField) {

            @Override
            public Field.Store store() {
                return Field.Store.YES;
            }

        };

        for (final Class<? extends IndexableFieldProcessor> aClass : searchableField.processors()) {

            final IndexableFieldProcessor<Object> indexableFieldProcessor =
                    indexableFieldProcessorProvider.get(fieldMetadata, aClass);

            final Document document = new Document();
            indexableFieldProcessor.process(document, value, fieldMetadata);

            for (final IndexableField indexableField : document.getFields()) {

                if (indexableField.stringValue() == null) {
                    continue;
                }

                final Term term = new Term(fieldMetadata.name(), indexableField.stringValue());
                booleanQuery.add(new TermQuery(term), BooleanClause.Occur.FILTER);

            }

        }

    }
}
