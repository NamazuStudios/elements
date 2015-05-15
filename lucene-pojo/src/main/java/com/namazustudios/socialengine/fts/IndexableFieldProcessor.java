package com.namazustudios.socialengine.fts;

import com.namazustudios.socialengine.fts.annotation.SearchableField;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.Query;

/**
 * Objects implementing this interface are used to generate any number of {@link IndexableField}
 * instances to be added to a {@link Document} or {@link Query}
 *
 * Created by patricktwohig on 5/12/15.
 */
public interface IndexableFieldProcessor<FieldT> {

    /**
     * Given {@link SearchableField} object, this converts the given fields to an
     * instance of {@link IndexableField}
     *
     *
     * @param document the document to which will receive the converted fields
     * @param value the fields read from the associated JXPath query
     * @param field the annotation
     *
     * @throws DocumentGeneratorException if the implementation opts to do so
     */
    void process(final Document document, final FieldT value, FieldMetadata field);

    interface Provider {

        /**
         * Used to generate instances of the {@link IndexableFieldProcessor} interface.
         *
         * @param searchableField the field metadata from the annotated field
         * @param implementationClass the implementation class
         *
         * @return an instance of {@link IndexableFieldProcessor}
         */
        <T> IndexableFieldProcessor<T> get(final FieldMetadata searchableField,
                                           Class<? extends IndexableFieldProcessor> implementationClass);

    }

}
