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
public class ObjectIdentityQuery<DocumentT> extends ObjectQuery<DocumentT> {

    private final Object identifier;

    private final IndexableFieldProcessor.Provider indexableFieldProcessorProvider;

    public ObjectIdentityQuery(Class<DocumentT> documentType,
                               Object identifier,
                               IndexableFieldProcessor.Provider indexableFieldProcessorProvider) {
        super(documentType);
        this.identifier = identifier;
        this.indexableFieldProcessorProvider = indexableFieldProcessorProvider;
    }

    @Override
    public Query getQuery() {

        Class<?> cls = getDocumentType();
        SearchableField searchableField = null;

        do {

            final SearchableIdentity searchableIdentity = getDocumentType().getAnnotation(SearchableIdentity.class);

            if (searchableIdentity != null) {
                searchableField = searchableIdentity.value();
                break;
            }

        } while (cls != null);

        if (searchableField == null) {
            throw new DocumentException("cannot find " +
                                        SearchableIdentity.class +
                                        " anywhere in the type" +
                                        " heirarchy for " +
                                        getDocumentType());
        }

        final BooleanQuery booleanQuery = new BooleanQuery();

        booleanQuery.add(getTypeQuery(), BooleanClause.Occur.FILTER);
        addTermsToQuery(booleanQuery, searchableField, identifier);

        return booleanQuery;

    }

    private void addTermsToQuery(final BooleanQuery booleanQuery,
                                 final SearchableField searchableField,
                                 final Object identifier) {

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
                booleanQuery.add(new TermQuery(term), BooleanClause.Occur.FILTER);

            }

        }

    }
}
