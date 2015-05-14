package com.namazustudios.socialengine.fts;

import com.namazustudios.socialengine.fts.annotation.SearchableField;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.document.Field.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default implementation for {@link IndexableFieldConverter}.  This will
 * process Object/primitive types to their respective {@link IndexableField}
 * implementation.
 *
 * This implementation indexes each type as follows:
 *
 * <ul>
 *  <li>byte - 32-bit integer {@link IntField}</li>
 *  <li>char - a single-character in {@link StringField} or {@link TextField} depending on the fields of {@link SearchableField#text()}</li>
 *  <li>short - 32-bit integer {@link IntField}</li>
 *  <li>int - 32-bit integer {@link IntField}</li>
 *  <li>long - 64-bit integer {@link LongField}</li>
 *  <li>float - 32-bit float {@link FloatField}</li>
 *  <li>double - 64-bit integer {@link DoubleField}</li>
 *  <li>byte[] - {@link Store#YES} is specified on {@link SearchableField#store()}, stored as {@link Field}</li>
 *  <li>char - {@link StringField} or {@link TextField} depending on the fields of {@link SearchableField#text()}</li>
 *  <li>{@link CharSequence} - {@link StringField} or {@link TextField} depending on the fields of {@link SearchableField#text()}</li>
 *  <li>{@link Iterable} - One instance of {@link IndexableField} for each element provided each element is compatible</li>
 * </ul>
 *
 * Anything else is logged as a warning.
 *
 */
public class DefaultIndexableFieldConverter implements IndexableFieldConverter<Object> {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultIndexableFieldConverter.class);

    @Override
    public void process(final Document document, final Object value, final FieldMetadata field) {

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
        } else if ((value instanceof byte[])) {

            // Added inside here to squelch log warning
            if (field.store().equals(Field.Store.YES)) {
                document.add(newStoredField((byte[])value, field));
            }

        } else if (value instanceof char[]) {
            document.add(newTextOrStringField(new String((char[])value), field));
        } else if (value instanceof CharSequence) {
            document.add(newTextOrStringField((CharSequence)value, field));
        } else if (value instanceof Iterable<?>) {
            for (final Object object : ((Iterable<?>)value)) {
                process(document, object, field);
            }
        } else if (value != null) {
            LOG.warn("Unable to fields of type " + value.getClass() +  "(" + value + ")");
        }

    }

    private IndexableField newStoredField(final byte[] value, final FieldMetadata field) {
        final Field out = new StoredField(field.name(), value);
        applyRemainingProperties(out, field);
        return out;
    }

    private IndexableField newIntegerField(final Number value, final FieldMetadata field) {
        final Field out = new IntField(field.name(), value.intValue(), field.store());
        applyRemainingProperties(out, field);
        return out;
    }

    private IndexableField newLongField(final Number value, final FieldMetadata field) {
        final Field out = new LongField(field.name(), value.longValue(), field.store());
        applyRemainingProperties(out, field);
        return out;
    }

    private IndexableField newFloatField(final Number value, final FieldMetadata field) {
        final Field out = new FloatField(field.name(), value.floatValue(), field.store());
        applyRemainingProperties(out, field);
        return out;
    }

    private IndexableField newDoubleField(final Number value, final FieldMetadata field) {
        final Field out = new DoubleField(field.name(), value.doubleValue(), field.store());
        applyRemainingProperties(out, field);
        return out;
    }

    private IndexableField newTextOrStringField(final Character value, final FieldMetadata field) {
        return newTextOrStringField(new String(new char[]{value.charValue()}), field);
    }

    private IndexableField newTextOrStringField(final CharSequence value, final FieldMetadata field) {

        final Field out;

        if (field.text()) {
            out = new TextField(field.name(), value.toString(), field.store());
        } else {
            out = new StringField(field.name(), value.toString(), field.store());
        }

        applyRemainingProperties(out, field);
        return out;
    }

    private void applyRemainingProperties(final Field field, final FieldMetadata searchableField) {

        if (searchableField.boost() != SearchableField.DEFAULT_BOOST) {
            // setBoost can result in an IllegalARgumentException, so this prevents that from
            // happening if the boost
            field.setBoost(searchableField.boost());
        }

    }

}
