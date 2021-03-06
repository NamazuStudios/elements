package com.namazustudios.socialengine.dao.mongo.provider;

import com.namazustudios.elements.fts.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

import javax.inject.Provider;
import java.lang.reflect.Proxy;

public class NullObjectIndexProvider implements Provider<ObjectIndex> {

    @Override
    public ObjectIndex get() {

        final var alwaysThrow = Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class[]{DocumentEntry.class},
            (proxy, method, args) -> {
                throw new UnsupportedOperationException();
            });

        return new ObjectIndex() {

            @Override
            public IOContext.Provider<IndexWriter> getIndexWriterContextProvider() {
                throw new UnsupportedOperationException();
            }

            @Override
            public IOContext.Provider<IndexSearcher> getIndexSearcherContextProvider() {
                throw new UnsupportedOperationException();
            }

            @Override
            public <T> DocumentEntry<T> index(T model) {
                return (DocumentEntry<T>) alwaysThrow;
            }

            @Override
            public <T> void delete(T model) {}

            @Override
            public <T> void delete(Class<T> type, Object identifier) {}

            @Override
            public <T> ObjectQuery<T> queryForType(Class<T> type) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <T> ObjectQuery<T> queryForIdentifier(Class<T> type, Object identifier) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <T> ObjectQuery<T> queryForObjects(Class<T> type, Query query) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <T> ObjectQuery<T> queryByExample(T object) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <T> QueryExecutor<T> execute(ObjectQuery<T> query) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <T> QueryExecutor<T> executeQueryForType(Class<T> type) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <T> QueryExecutor<T> executeQueryForIdentifier(Class<T> type, Object identifier) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <T> QueryExecutor<T> executeQueryForObjects(Class<T> type, Query query) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <T> QueryExecutor<T> executeQueryByExample(T object) {
                throw new UnsupportedOperationException();
            }

        };
    }

}
