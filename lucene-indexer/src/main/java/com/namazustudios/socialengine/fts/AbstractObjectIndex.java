package com.namazustudios.socialengine.fts;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

import java.io.IOException;

/**
 *
 * Created by patricktwohig on 5/14/15.
 */
public abstract class AbstractObjectIndex implements ObjectIndex {

    private final DocumentGenerator documentGenerator;

    private final IOContext.Provider<IndexWriter> indexWriterContextProvider;

    private final IOContext.Provider<IndexSearcher> indexSearcherContextProvider;

    public AbstractObjectIndex(DocumentGenerator documentGenerator,
                               IOContext.Provider<IndexWriter> indexWriterContextProvider,
                               IOContext.Provider<IndexSearcher> indexSearcherContextProvider) {
        this.documentGenerator = documentGenerator;
        this.indexWriterContextProvider = indexWriterContextProvider;
        this.indexSearcherContextProvider = indexSearcherContextProvider;
    }

    @Override
    public <T> DocumentEntry<T> index(Class<T> type, T model) {

        final DocumentEntry<T> documentEntry = documentGenerator.generate(model);
        final ObjectQuery<T> queryByExample = queryByExample(type, model);

        try (final IOContext<IndexWriter> indexWriterIOContext = indexWriterContextProvider.get()) {
            indexWriterIOContext.instance().deleteDocuments(queryByExample.getQuery());
            indexWriterIOContext.instance().addDocument(documentEntry.getDocument());
            indexWriterIOContext.instance().commit();
            return documentEntry;
        } catch (IOException ex) {
            throw new SearchException(ex);
        }

    }

    @Override
    public <T> void delete(Class<T> type, T model) {

        final ObjectQuery<?> queryByExample = queryByExample(type, model);

        try (final IOContext<IndexWriter> indexWriterIOContext = indexWriterContextProvider.get()) {
            indexWriterIOContext.instance().deleteDocuments(queryByExample.getQuery());
            indexWriterIOContext.instance().commit();
        } catch (IOException ex) {
            throw new SearchException(ex);
        }

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

    @Override
    public <T> QueryExecutor<T> execute(ObjectQuery<T> query) {
        return new QueryExecutor<>(documentGenerator, indexSearcherContextProvider.get(), query);
    }

    @Override
    public <T> QueryExecutor<T> executeQueryForType(Class<T> type) {
        return execute(queryForType(type));
    }

    @Override
    public <T> QueryExecutor<T> executeQueryForIdentifier(Class<T> type, Object identifier) {
        return execute(queryForIdentifier(type, identifier));
    }

    @Override
    public <T> QueryExecutor<T> executeQueryForObjects(Class<T> type, Query query) {
        return execute(queryForObjects(type, query));
    }

    @Override
    public <T> QueryExecutor<T> executeQueryByExample(Class<T> type, T object) {
        return execute(queryByExample(type, object));
    }

}
