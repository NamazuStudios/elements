package com.namazustudios.socialengine.fts;

import org.apache.lucene.document.Document;

/**
 * Given an instance, annotated with th
 * Created by patricktwohig on 5/12/15.
 */
public interface DocumentGenerator {

    /**
     * Analyzes the given {@link Class}, searching for the presence of
     * the {@link com.namazustudios.socialengine.fts.annotation.SearchableDocument} annotation
     * generating an index of the fields.
     *
     * @param cls
     * @return a {@link ContextProcessor} which can be used to proces a context and Document
     */
    ContextProcessor analyze(final Class<?> cls);

    /**
     * Generates a {@link Document} from the given object.
     *
     * @param object the object to index, not null
     * @return a Document which can be written to the search index.
     *
     */
    <DocumentT> DocumentEntry<? extends DocumentT, ?> generate(final DocumentT object);

    /***
     * Processes the given object, adding all found {@link org.apache.lucene.index.IndexableField}
     * instances to it.
     *
     * @param object the Object to index, not null
     * @param document the target document
     *
     */
    <DocumentT> DocumentEntry<? extends DocumentT, ?> process(final DocumentT object, final Document document);

}
