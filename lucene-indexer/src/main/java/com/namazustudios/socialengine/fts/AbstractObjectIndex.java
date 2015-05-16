package com.namazustudios.socialengine.fts;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

import javax.inject.Inject;

/**
 *
 * Created by patricktwohig on 5/14/15.
 */
public abstract class AbstractObjectIndex implements ObjectIndex {

    private final DocumentGenerator documentGenerator;

    private final IndexWriter indexWriter;

    private final IndexReader indexReader;

    private final IndexSearcher indexSearcher;

    public AbstractObjectIndex(DocumentGenerator documentGenerator,
                               IndexWriter indexWriter,
                               IndexReader indexReader,
                               IndexSearcher indexSearcher) {
        this.documentGenerator = documentGenerator;
        this.indexWriter = indexWriter;
        this.indexReader = indexReader;
        this.indexSearcher = indexSearcher;
    }

    @Override
    public <T> DocumentEntry<T> index(T model) {
        final DocumentEntry<T> documentEntry = documentGenerator.generate(model);
        return null;
    }

    @Override
    public void delete(Object model) {

    }

    @Override
    public <T> ObjectQuery<T> queryForType(Class<T> type) {
        return new WildcardObjectQuery<>(type, documentGenerator.getIndexableFieldProcessorProvider());
    }

    @Override
    public <T> ObjectQuery<T> queryForIdentifier(Class<T> type, Object identifier) {
        return new IdentityObjectQuery<>(type, documentGenerator.getIndexableFieldProcessorProvider(), identifier);
    }

    @Override
    public <T> ObjectQuery<T> queryForObjects(Class<T> type, Query query) {
        return new ArbitraryObjectQuery<>(type, documentGenerator.getIndexableFieldProcessorProvider(), query);
    }

    @Override
    public <T> ObjectQuery<T> queryByExample(Class<T> type, T object) {
        return new ExampleObjectQuery<>(type, documentGenerator.getIndexableFieldProcessorProvider(), object);
    }

}
