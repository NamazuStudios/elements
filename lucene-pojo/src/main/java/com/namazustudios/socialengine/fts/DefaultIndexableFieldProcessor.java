package com.namazustudios.socialengine.fts;

import com.namazustudios.socialengine.fts.annotation.DefaultType;
import com.namazustudios.socialengine.fts.annotation.SearchableField;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The default implementation for {@link IndexableFieldProcessor}.  This will
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
 *  <li>boolean - {@link StringField} stored with {@link Boolean#toString()}</li>
 *  <li>String - {@link StringField} or {@link TextField} depending on the value of {@link SearchableField#text()}</li>
 *  <li>{@link CharSequence} - {@link StringField} or {@link TextField} depending on the fields of {@link SearchableField#text()}</li>
 *  <li>{@link Iterable} - One instance of {@link IndexableField} for each element provided each element is compatible</li>
 *  <li>{@link Class} - Stores the Class FQN as a string</li>
 *  <li>{@link Enum} - Stores the enum value as a string using the {@link Enum#toString()}</li>
 * </ul>
 *
 * Anything else is logged as a warning.
 *
 */
public class DefaultIndexableFieldProcessor extends AbstractIndexableFieldProcessor<Object> {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultIndexableFieldProcessor.class);

    @Override
    public void process(final Document document, final Object value, final FieldMetadata fieldMetadata) {

        final Class<?> type = fieldMetadata.type();

        if (value != null && !type.equals(DefaultType.class) && !type.isInstance(value)) {
            throw new DocumentGenerationException(document, value, fieldMetadata,
                    "type mismatch for " + fieldMetadata + " got " + value + " instead");
        }

        for (final Field field : generateFields(value, fieldMetadata)) {
            document.removeFields(field.name());
            document.add(field);
        }

    }

    private List<Field> generateFields(final Object value, final FieldMetadata fieldMetadata) {
        final List<Field> fields = new ArrayList<>();
        generateFields(value, fieldMetadata, fields);
        return fields;
    }

    private void generateFields(final Object value,
                                final FieldMetadata fieldMetadata,
                                final List<Field> fields) {

        if (value instanceof Byte) {
            fields.add(newIntegerField((Byte) value, fieldMetadata));
        } else if (value instanceof Character) {
            fields.add(newTextOrStringField((Character) value, fieldMetadata));
        } else if (value instanceof Short) {
            fields.add(newIntegerField((Short) value, fieldMetadata));
        } else if (value instanceof Integer) {
            fields.add(newIntegerField((Integer) value, fieldMetadata));
        } else if (value instanceof Long) {
            fields.add(newLongField((Long) value, fieldMetadata));
        } else if (value instanceof Float) {
            fields.add(newFloatField((Float) value, fieldMetadata));
        } else if (value instanceof Double) {
            fields.add(newDoubleField((Double) value, fieldMetadata));
        } else if (value instanceof Boolean) {
            fields.add(newStringField(value.toString(), fieldMetadata));
        } else if ((value instanceof byte[]) && fieldMetadata.store().equals(Field.Store.YES)) {
            fields.add(newStoredField((byte[]) value, fieldMetadata));
        } else if (value instanceof char[]) {
            fields.add(newTextOrStringField(new String((char[]) value), fieldMetadata));
        } else if (value instanceof CharSequence) {
            fields.add(newTextOrStringField((CharSequence) value, fieldMetadata));
        } else if (value instanceof Iterable<?>) {
            for (final Object object : ((Iterable<?>) value)) {
                generateFields(object, fieldMetadata, fields);
            }
        } else if (value instanceof Enum<?>) {
            fields.add(newTextOrStringField(value.toString(), fieldMetadata));
        } else if (value instanceof Class<?>) {
            final Class<?> cls = (Class<?>)value;
            fields.add(newStringField(cls.getName(), fieldMetadata));
        } else if (value != null) {
            LOG.warn("Unable to process field " + fieldMetadata +  "(" + value + ")");
        }

    }

}
