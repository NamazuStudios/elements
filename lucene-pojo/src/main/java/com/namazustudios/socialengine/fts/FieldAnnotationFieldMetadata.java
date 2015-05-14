package com.namazustudios.socialengine.fts;

import com.namazustudios.socialengine.fts.annotation.SearchableField;
import org.apache.lucene.document.Field;

/**
 * Created by patricktwohig on 5/13/15.
 */
public class FieldAnnotationFieldMetadata implements FieldMetadata {

    private final SearchableField searchableField;

    public FieldAnnotationFieldMetadata(SearchableField searchableField) {
        this.searchableField = searchableField;
    }

    @Override
    public Field.Store store() {
        return searchableField.store();
    }

    @Override
    public boolean text() {
        return searchableField.text();
    }

    @Override
    public Class<? extends IndexableFieldProcessor> processor() {
        return searchableField.converter();
    }

    @Override
    public float boost() {
        return searchableField.boost();
    }

    @Override
    public String name() {
        return searchableField.name();
    }

    @Override
    public String path() {
        return searchableField.path();
    }

}
