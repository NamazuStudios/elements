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

    private final IndexWriter indexWriter;

    private final IndexSearcher indexSearcher;

    public AbstractObjectIndex(DocumentGenerator documentGenerator,
                               IndexWriter indexWriter,
                               IndexSearcher indexSearcher) {
        this.documentGenerator = documentGenerator;
        this.indexWriter = indexWriter;
        this.indexSearcher = indexSearcher;
    }

    @Override
    public <T> DocumentEntry<T> index(Class<T> type, T model) {

        final DocumentEntry<T> documentEntry = documentGenerator.generate(model);
        final ObjectQuery<T> queryByExample = queryByExample(type, model);

        try {
            indexWriter.deleteDocuments(queryByExample.getQuery());
            indexWriter.addDocument(documentEntry.getDocument());
            indexWriter.commit();
            return documentEntry;
        } catch (IOException ex) {
            throw new SearchException(ex);
        }

    }

    @Override
    public <T> void delete(Class<T> type, T model) {

        final ObjectQuery<?> queryByExample = queryByExample(type, model);

        try {
            indexWriter.deleteDocuments(queryByExample.getQuery());
            indexWriter.commit();
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
    public <T> QueryExecutor<T> perform(ObjectQuery<T> query) {

        return new QueryExecutor<>(documentGenerator, indexSearcher, query);
    }


}


class Test {

    class ClassTHolder<ClassT> {

        private final Class<ClassT> cls;

        public ClassTHolder(Class<ClassT> cls) {
            this.cls = cls;
        }

        public Class<ClassT> getCls() {
            return cls;
        }

        public ClassT cast(Object o) {
            return cls.cast(o);
        }

    }

    public <T> T get(Class<T> cls, Object o) {
        return cls.cast(o);
    }

    public <T> T get(ClassTHolder<T> holder, Object o) {
        Class<T> cls = holder.getCls();
        return get(cls, o);
    }


}
