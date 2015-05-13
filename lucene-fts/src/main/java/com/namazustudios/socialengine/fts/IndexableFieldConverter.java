package com.namazustudios.socialengine.fts;

import com.namazustudios.socialengine.fts.annotation.SearchableField;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;

/**
 * Created by patricktwohig on 5/12/15.
 */
public interface IndexableFieldConverter<FieldT> {

    /**
     * Given {@link SearchableField} object, this converts the given value to an
     * instance of {@link IndexableField}
     *
     *
     * @param document the document to which will receive the converted fields
     * @param value the value read from the associated JXPath query
     * @param field the annotation
     *
     * @return a list containing zero or more fields produced, this may never return null
     *
     */
    void process(final Document document, final FieldT value, SearchableField field);

}
