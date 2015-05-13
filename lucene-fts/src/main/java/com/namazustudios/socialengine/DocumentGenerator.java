package com.namazustudios.socialengine;

import org.apache.lucene.document.Document;

/**
 * Given an instance, annotated with th
 * Created by patricktwohig on 5/12/15.
 */
public interface DocumentGenerator {

    /**
     * Analyzes the given {@link Class}, searching for the presence of
     * the {@link com.namazustudios.socialengine.annotation.SearchableDocument} annotation
     * generating an index of the fields.
     *
     * @param cls
     */
    void analyze(final Class<?> cls);

    /**
     * Generates a {@link Document} from the given object.
     *
     * @param object the object, not null
     * @return a Document which can be written to the search index.
     *
     */
    Document generate(final Object object);

}
