package com.namazustudios.socialengine.fts;

import com.namazustudios.socialengine.fts.annotation.DefaultType;
import com.namazustudios.socialengine.fts.annotation.SearchableField;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexableField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * The default implementation for {@link IndexableFieldProcessor}.  This will
 * process Object/primitive types to their respective {@link IndexableField}
 * implementation.
 *
 * This implementation indexes each type as follows:
 *
 * <ul>
 *  <li>byte - 32-bit integer {@link IntPoint}</li>
 *  <li>char - a single-character in {@link StringField} or {@link TextField} depending on the fields of {@link SearchableField#text()}</li>
 *  <li>short - 32-bit integer {@link IntPoint}</li>
 *  <li>int - 32-bit integer {@link IntPoint}</li>
 *  <li>long - 64-bit integer {@link LongPoint}</li>
 *  <li>float - 32-bit float {@link FloatPoint}</li>
 *  <li>double - 64-bit integer {@link DoublePoint}</li>
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

        document.removeFields(fieldMetadata.name());
        generateFields(value, fieldMetadata, document::add);

    }

    private void generateFields(final Object value,
                                final FieldMetadata fieldMetadata,
                                final Consumer<Field> fieldConsumer) {

        if (value instanceof Byte) {
            newIntegerFields(fieldConsumer, (Byte) value, fieldMetadata);
        } else if (value instanceof Character) {
            newTextOrStringField(fieldConsumer, (Character) value, fieldMetadata);
        } else if (value instanceof Short) {
            newIntegerFields(fieldConsumer, (Short) value, fieldMetadata);
        } else if (value instanceof Integer) {
            newIntegerFields(fieldConsumer, (Integer) value, fieldMetadata);
        } else if (value instanceof Long) {
            newLongFields(fieldConsumer, (Long) value, fieldMetadata);
        } else if (value instanceof Float) {
            newFloatFields(fieldConsumer, (Float) value, fieldMetadata);
        } else if (value instanceof Double) {
            newDoubleFields(fieldConsumer, (Double) value, fieldMetadata);
        } else if (value instanceof Boolean) {
            newStringFields(fieldConsumer, value.toString(), fieldMetadata);
        } else if ((value instanceof byte[]) && fieldMetadata.store().equals(Field.Store.YES)) {
            newStoredField(fieldConsumer, (byte[]) value, fieldMetadata);
        } else if (value instanceof char[]) {
            newTextOrStringField(fieldConsumer, new String((char[]) value), fieldMetadata);
        } else if (value instanceof CharSequence) {
            newTextOrStringField(fieldConsumer, (CharSequence) value, fieldMetadata);
        } else if (value instanceof Iterable<?>) {
            for (final Object object : ((Iterable<?>) value)) {
                generateFields(object, fieldMetadata, fieldConsumer);
            }
        } else if (value instanceof Enum<?>) {
            newTextOrStringField(fieldConsumer, value.toString(), fieldMetadata);
        } else if (value instanceof Class<?>) {
            final Class<?> cls = (Class<?>)value;
            newStringFields(fieldConsumer, cls.getName(), fieldMetadata);
        } else if (value != null) {
            LOG.warn("Unable to process field " + fieldMetadata +  "(" + value + ")");
        }

    }

}
