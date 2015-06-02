package com.namazustudios.socialengine.fts;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

import java.io.IOException;

/**
 * The abstract implementation of the {@link ObjectIndex}.  This includes the basic functionality.
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
    public IOContext.Provider<IndexWriter> getIndexWriterContextProvider() {
        return indexWriterContextProvider;
    }

    @Override
    public IOContext.Provider<IndexSearcher> getIndexSearcherContextProvider() {
        return indexSearcherContextProvider;
    }

    @Override
    public <T> DocumentEntry<T> index(T model) {

        final DocumentEntry<T> documentEntry = documentGenerator.generate(model);
        final ObjectQuery<T> queryByExample = queryByExample(model);

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
    public <T> void delete(T model) {

        final ObjectQuery<?> queryByExample = queryByExample(model);

        try (final IOContext<IndexWriter> indexWriterIOContext = indexWriterContextProvider.get()) {
            indexWriterIOContext.instance().deleteDocuments(queryByExample.getQuery());
            indexWriterIOContext.instance().commit();
        } catch (IOException ex) {
            throw new SearchException(ex);
        }

    }

    @Override
    public <T> void delete(Class<T> type, Object identifier) {

        final ObjectQuery<T> objectQuery = queryForIdentifier(type, identifier);

        try (final IOContext<IndexWriter> indexWriterIOContext = indexWriterContextProvider.get()) {
            indexWriterIOContext.instance().deleteDocuments(objectQuery.getQuery());
            indexWriterIOContext.instance().commit();
        } catch (IOException ex) {
            throw new SearchException(ex);
        }

    }

    @Override
    public <T> ObjectQuery<T> queryForType(final Class<T> type) {
        return new WildcardObjectQuery<>(type, documentGenerator.getIndexableFieldProcessorProvider());
    }

    @Override
    public <T> ObjectQuery<T> queryForIdentifier(final Class<T> type, final Object identifier) {
        return new IdentityObjectQuery<>(type, documentGenerator.getIndexableFieldProcessorProvider(), identifier);
    }

    @Override
    public <T> ObjectQuery<T> queryForObjects(final Class<T> type, final Query query) {
        return new ArbitraryObjectQuery<>(type, documentGenerator.getIndexableFieldProcessorProvider(), query);
    }

    @Override
    public <T> ObjectQuery<T> queryByExample(final T object) {
        final Class<? extends T> type = (Class<? extends T>)object.getClass();
        return new ExampleObjectQuery<>(type, documentGenerator.getIndexableFieldProcessorProvider(), object);
    }

    @Override
    public <T> QueryExecutor<T> execute(ObjectQuery<T> query) {
        try {
            return new QueryExecutor<>(documentGenerator, indexSearcherContextProvider.get(), query);
        } catch (IOException ex) {
            throw new SearchException(ex);
        }
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
    public <T> QueryExecutor<T> executeQueryByExample(T object) {
        return execute(queryByExample(object));
    }

}
