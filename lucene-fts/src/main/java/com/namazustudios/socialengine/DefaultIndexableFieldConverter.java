package com.namazustudios.socialengine;

import java.util.ArrayList;
import java.util.List;
import com.namazustudios.socialengine.annotation.SearchableField;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexableField;

import java.util.Arrays;

/**
 * The default implementation for {@link IndexableFieldConverter}.  This will
 * convert Object/primitive types to their respective {@link IndexableField}
 * implementation.
 */
public class DefaultIndexableFieldConverter implements IndexableFieldConverter<Object> {

    @Override
    public List<IndexableField> convert(Object value, SearchableField field) {

        final List<IndexableField> fieldList = new ArrayList<>();

        if (value instanceof Byte) {
            fieldList.add(newIntegerField((Byte) value, field));
        } else if (value instanceof Character) {
            fieldList.add(newTextOrStringField((Character)value, field));
        } else if (value instanceof Short) {
            fieldList.add(newIntegerField((Short)value, field));
        } else if (value instanceof Integer) {
            fieldList.add(newIntegerField((Integer)value, field));
        } else if (value instanceof Long) {
            fieldList.add(newLongField((Long) value, field));
        } else if (value instanceof Float) {
            fieldList.add(newFloatField((Float)value, field));
        } else if (value instanceof Double) {
            fieldList.add(newDoubleField((Float)value, field));
        } else if ((value instanceof byte[]) && field.store().equals(Field.Store.YES)) {
            fieldList.add(newStoredField((byte[])value, field));
        } else if (value instanceof char[]) {
            fieldList.add(newTextOrStringField(new String((char[])value), field));
        } else if (value instanceof CharSequence) {
            fieldList.add(newTextOrStringField((CharSequence)value, field));
        } else if (value instanceof Iterable<?>) {
            for (final Object object : ((Iterable<?>)value)) {
                fieldList.addAll(convert(object, field));
            }
        }

        return fieldList;

    }

    private IndexableField newStoredField(final byte[] value, SearchableField field) {
        return new StoredField(field.name(), value);
    }

    private IndexableField newIntegerField(final Number value, SearchableField field) {
        return new IntField(field.name(), value.intValue(), field.store());
    }

    private IndexableField newLongField(final Number value, SearchableField field) {
        return new LongField(field.name(), value.longValue(), field.store());
    }

    private IndexableField newFloatField(final Number value, SearchableField field) {
        return new FloatField(field.name(), value.floatValue(), field.store());
    }

    private IndexableField newDoubleField(final Number value, SearchableField field) {
        return new DoubleField(field.name(), value.doubleValue(), field.store());
    }

    private IndexableField newTextOrStringField(final Character value, SearchableField field) {
        return newTextOrStringField(new String(new char[]{value.charValue()}), field);
    }

    private IndexableField newTextOrStringField(final CharSequence value, SearchableField field) {
        if (field.text()) {
            return new TextField(field.name(), value.toString(), field.store());
        } else {
            return new StringField(field.name(), value.toString(), field.store());
        }
    }

}
