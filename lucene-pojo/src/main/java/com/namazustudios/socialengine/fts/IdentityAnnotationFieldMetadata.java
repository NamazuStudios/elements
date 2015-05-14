package com.namazustudios.socialengine.fts;

import com.namazustudios.socialengine.fts.annotation.SearchableField;
import com.namazustudios.socialengine.fts.annotation.SearchableIdentity;
import org.apache.lucene.document.Field;

/**
 * Created by patricktwohig on 5/14/15.
 */
public class IdentityAnnotationFieldMetadata implements FieldMetadata {

    private final SearchableIdentity searchableIdentity;

    public IdentityAnnotationFieldMetadata(final SearchableIdentity searchableIdentity) {
        this.searchableIdentity = searchableIdentity;
    }

    @Override
    public Field.Store store() {
        return Field.Store.YES;
    }

    @Override
    public boolean text() {
        return false;
    }

    @Override
    public Class<? extends IndexableFieldConverter> converter() {
        return searchableIdentity.converter();
    }

    @Override
    public float boost() {
        return SearchableField.DEFAULT_BOOST;
    }

    @Override
    public String name() {
        return SearchableIdentity.CLASS_FIELD_NAME;
    }

    @Override
    public String path() {
        return searchableIdentity.path();
    }

}
