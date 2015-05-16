package com.namazustudios.socialengine.fts;

import com.namazustudios.socialengine.fts.annotation.SearchableDocument;
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
 * Wrapper around a Lucene {@link Query} for a specific type in the search index.  This class
 * can be used as the base query for a type, and additionally this can be used to find
 * a specific object in the index.
 *
 * The specific-object query is used internally in managing the index.
 *
 * Created by patricktwohig on 5/15/15.
 */
public class TypeQuery<DocumentT> {

    private final Query query;

    private final Class<DocumentT> documentType;

    private final ObjectIndex objectIndex;

    public TypeQuery(final ObjectIndex objectIndex, final Class<DocumentT> documentType) {

        final SearchableDocument searchableDocument = documentType.getAnnotation(SearchableDocument.class);

        if (searchableDocument == null) {
            throw new IllegalArgumentException( documentType + " does not have annotation " + SearchableDocument.class);
        }

        final Term term = new Term(searchableDocument.type().name(), documentType.getName());

        this.query = new TermQuery(term);
        this.documentType = documentType;
        this.objectIndex = objectIndex;

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
     * The raw Query object itself.
     *
     * @return the raw query, untyped.
     */
    public Query getQuery() {
        return query;
    }

    /**
     *
     * @param identifier
     * @return
     */
    public Query getQueryForIdentifier(Object identifier) {

        Class<?> cls = documentType;
        SearchableField searchableField = null;

        do {

            final SearchableIdentity searchableIdentity = documentType.getAnnotation(SearchableIdentity.class);

            if (searchableIdentity != null) {
                searchableField = searchableIdentity.value();
                break;
            }

        } while (cls != null);

        if (searchableField == null) {
            throw new DocumentException("cannot find " + SearchableIdentity.class + " anywhere in the type " +
                    "heirarchy for " + documentType);

        }

        final BooleanQuery booleanQuery = new BooleanQuery();

        booleanQuery.add(getQuery(), BooleanClause.Occur.MUST);
        addTermsToQuery(booleanQuery, searchableField, identifier);

        return booleanQuery;

    }

    private void addTermsToQuery(final BooleanQuery booleanQuery,
                                 final SearchableField searchableField,
                                 final Object identifier) {

        final IndexableFieldProcessor.Provider indexableFieldProcessorProvider = objectIndex
                .getDocumentGenerator()
                .getIndexableFieldProcessorProvider();

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
            indexableFieldProcessor.process(document, identifier, fieldMetadata);

            for (final IndexableField indexableField : document.getFields()) {

                if (indexableField.stringValue() == null) {
                    continue;
                }

                final Term term = new Term(fieldMetadata.name(), indexableField.stringValue());
                booleanQuery.add(new TermQuery(term), BooleanClause.Occur.MUST);

            }

        }

    }

}
