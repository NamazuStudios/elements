package com.namazustudios.socialengine.fts;

import com.namazustudios.socialengine.fts.annotation.SearchableField;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexableField;

/**
 * The default implementation for {@link IndexableFieldConverter}.  This will
 * process Object/primitive types to their respective {@link IndexableField}
 * implementation.
 */
public class DefaultIndexableFieldConverter implements IndexableFieldConverter<Object> {

    @Override
    public void process(final Document document, final Object value, final SearchableField field) {

        if (value instanceof Byte) {
            document.add(newIntegerField((Byte) value, field));
        } else if (value instanceof Character) {
            document.add(newTextOrStringField((Character)value, field));
        } else if (value instanceof Short) {
            document.add(newIntegerField((Short)value, field));
        } else if (value instanceof Integer) {
            document.add(newIntegerField((Integer)value, field));
        } else if (value instanceof Long) {
            document.add(newLongField((Long) value, field));
        } else if (value instanceof Float) {
            document.add(newFloatField((Float)value, field));
        } else if (value instanceof Double) {
            document.add(newDoubleField((Float)value, field));
        } else if ((value instanceof byte[]) && field.store().equals(Field.Store.YES)) {
            document.add(newStoredField((byte[])value, field));
        } else if (value instanceof char[]) {
            document.add(newTextOrStringField(new String((char[])value), field));
        } else if (value instanceof CharSequence) {
            document.add(newTextOrStringField((CharSequence)value, field));
        } else if (value instanceof Iterable<?>) {
            for (final Object object : ((Iterable<?>)value)) {
                process(document, object, field);
            }
        }

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
