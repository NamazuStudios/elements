package com.namazustudios.socialengine;

import com.namazustudios.socialengine.annotation.SearchableField;
import org.apache.lucene.index.IndexableField;

import java.util.List;

/**
 * Created by patricktwohig on 5/12/15.
 */
public interface IndexableFieldConverter<FieldT> {

    /**
     * Given {@link SearchableField} object, this converts the given value to an
     * instance of {@link IndexableField}
     *
     *
     * @param value the value read from the associated JXPath query
     * @param field the annotation
     *
     * @return a list containing zero or more fields produced, this may never return null
     *
     */
    List<IndexableField> convert(final FieldT value, SearchableField field);

}
