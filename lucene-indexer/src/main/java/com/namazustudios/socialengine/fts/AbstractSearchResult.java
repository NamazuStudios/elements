package com.namazustudios.socialengine.fts;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Pools some of the common functionality for all instances of {@link SearchResult}.
 *
 * Created by patricktwohig on 5/16/15.
 */
public abstract class AbstractSearchResult<DocumentT, EntryT extends DocumentEntry<DocumentT>>
        implements SearchResult<DocumentT, EntryT> {

    protected final ObjectQuery objectQuery;

    protected final IOContext<IndexSearcher> indexSearcherIOContext;

    protected final DocumentGenerator documentGenerator;

    public AbstractSearchResult(final ObjectQuery objectQuery,
                                final DocumentGenerator documentGenerator,
                                final IOContext<IndexSearcher> indexSearcherIOContext) {
        this.objectQuery = objectQuery;
        this.documentGenerator = documentGenerator;
        this.indexSearcherIOContext = indexSearcherIOContext;
    }

    protected DocumentEntry<DocumentT> getEntry(final int doc) {

        final Class<DocumentT> cls = objectQuery.getDocumentType();

        final Document document;

        try {
            document = indexSearcherIOContext.instance().getIndexReader().document(doc);
        } catch (IOException ex) {
            throw new SearchException(ex);
        }

        return documentGenerator.entry(cls, document);

    }

    @Override
    public SearchResult<DocumentT, EntryT> prune(final int count) {

        if (count < 0) {
            throw new IllegalArgumentException("count must be positive");
        }

        final int pruned = Math.min(count, available());

        return new SearchResult<DocumentT, EntryT>() {

            @Override
            public int total() {
                return AbstractSearchResult.this.total();
            }

            @Override
            public int available() {
                return pruned;
            }

            @Override
            public DocumentEntry<DocumentT> singleResult() {

                final Iterator<EntryT> itr = iterator();

                if (!itr.hasNext()) {
                    throw new NoResultException();
                }

                final EntryT out = itr.next();

                if (itr.hasNext()) {
                    throw new MultipleResultException();
                }

                return out;

            }

            @Override
            public SearchResult<DocumentT, EntryT> prune(int count) {
                return AbstractSearchResult.this.prune(count);
            }

            @Override
            public Iterator<EntryT> iterator() {
                return new Iterator<EntryT>() {

                    int count = 0;

                    final Iterator<EntryT> wrapped = AbstractSearchResult.this.iterator();

                    @Override
                    public boolean hasNext() {
                        return count < pruned;
                    }

                    @Override
                    public EntryT next() {

                        if (count++ >= pruned) {
                            throw new NoSuchElementException();
                        }

                        return wrapped.next();

                    }

                    @Override
                    public void remove() {
                        wrapped.remove();
                    }

                    @Override
                    public String toString() {
                        return "delegates to " + wrapped;
                    }

                };
            }

            @Override
            public String toString() {
                return "delegates to " + AbstractSearchResult.this;
            }

        };
    }

    @Override
    public String toString() {
        return "TopDocsSearchResult{" +
                "objectQuery=" + objectQuery +
                '}';
    }

}
