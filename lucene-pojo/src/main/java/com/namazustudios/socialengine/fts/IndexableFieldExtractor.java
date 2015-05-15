package com.namazustudios.socialengine.fts;

import org.apache.lucene.document.Document;

/**
 * Created by patricktwohig on 5/14/15.
 */
public interface IndexableFieldExtractor<FieldT> {

    /**
     * Extracts the identity
     *
     * @param document
     * @param fieldMetadata
     * @return
     */
    FieldT extract(final Document document, FieldMetadata fieldMetadata);

    interface Provider {

        /**
         * Used to generate instances of the {@link IndexableFieldExtractor} interface.
         *
         * @return an instance of {@link IndexableFieldExtractor}
         */
        <T> IndexableFieldExtractor<T> get(final FieldMetadata searchableField);

    }

}
