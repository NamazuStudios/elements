package com.namazustudios.socialengine.fts;

import com.namazustudios.socialengine.fts.annotation.SearchableField;
import org.apache.lucene.document.Field;

import java.lang.annotation.Annotation;

/**
 * Created by patricktwohig on 5/13/15.
 */
public class AnnotationFieldMetadata implements FieldMetadata {

    private final SearchableField searchableField;

    public AnnotationFieldMetadata(SearchableField searchableField) {
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
    public Class<? extends IndexableFieldProcessor>[] processors() {
        return searchableField.processors();
    }

    @Override
    public Class<? extends IndexableFieldExtractor> extractor() {
        return searchableField.extractor();
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

    @Override
    public Class<?> type() {
        return searchableField.type();
    }

    @Override
    public String toString() {
        return searchableField.toString();
    }

}
