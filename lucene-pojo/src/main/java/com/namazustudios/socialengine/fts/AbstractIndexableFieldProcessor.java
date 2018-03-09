package com.namazustudios.socialengine.fts;

import com.namazustudios.socialengine.fts.annotation.SearchableField;
import org.apache.lucene.document.*;

import java.util.function.Consumer;

/**
 * Created by patricktwohig on 6/1/15.
 */
public abstract class AbstractIndexableFieldProcessor<FieldT> implements IndexableFieldProcessor<FieldT> {

    protected void newStoredField(final Consumer<Field> fieldConsumer,
                                  final byte[] value,
                                  final FieldMetadata fieldMetadata) {
        final Field out = new StoredField(fieldMetadata.name(), value);
        applyRemainingProperties(out, fieldMetadata);
        fieldConsumer.accept(out);
    }

    protected void newIntegerFields(final Consumer<Field> fieldConsumer,
                                    final Number value,
                                    final FieldMetadata fieldMetadata) {
        switch (fieldMetadata.store()) {
            case YES:
                fieldConsumer.andThen(f -> applyRemainingProperties(f, fieldMetadata))
                             .accept(new StoredField(fieldMetadata.name(), value.intValue()));
            case NO:
                fieldConsumer.andThen(f -> applyRemainingProperties(f, fieldMetadata))
                             .accept(new IntPoint(fieldMetadata.name(), value.intValue()));
                break;
            default:
                throw new IllegalArgumentException("Invalid store value: " + fieldMetadata.store());
        }
    }

    protected void newLongFields(final Consumer<Field> fieldConsumer,
                                 final Number value,
                                 final FieldMetadata fieldMetadata) {

        switch (fieldMetadata.store()) {
            case YES:
                fieldConsumer.andThen(f -> applyRemainingProperties(f, fieldMetadata))
                             .accept(new StoredField(fieldMetadata.name(), value.longValue()));
            case NO:
                fieldConsumer.andThen(f -> applyRemainingProperties(f, fieldMetadata))
                             .accept(new LongPoint(fieldMetadata.name(), value.longValue()));
                break;
            default:
                throw new IllegalArgumentException("Invalid store value: " + fieldMetadata.store());

        }
    }

    protected void newFloatFields(final Consumer<Field> fieldConsumer,
                                  final Number value,
                                  final FieldMetadata fieldMetadata) {

        switch (fieldMetadata.store()) {
            case YES:
                fieldConsumer.andThen(f -> applyRemainingProperties(f, fieldMetadata))
                             .accept(new StoredField(fieldMetadata.name(), value.floatValue()));
            case NO:
                fieldConsumer.andThen(f -> applyRemainingProperties(f, fieldMetadata))
                             .accept(new FloatPoint(fieldMetadata.name(), value.floatValue()));
                break;
            default:
                throw new IllegalArgumentException("Invalid store value: " + fieldMetadata.store());

        }

    }

    protected void newDoubleFields(final Consumer<Field> fieldConsumer,
                                   final Number value,
                                   final FieldMetadata fieldMetadata) {

        switch (fieldMetadata.store()) {
            case YES:
                fieldConsumer.andThen(f -> applyRemainingProperties(f, fieldMetadata))
                             .accept(new StoredField(fieldMetadata.name(), value.doubleValue()));
            case NO:
                fieldConsumer.andThen(f -> applyRemainingProperties(f, fieldMetadata))
                             .accept(new DoublePoint(fieldMetadata.name(), value.doubleValue()));
                break;
            default:
                throw new IllegalArgumentException("Invalid store value: " + fieldMetadata.store());

        }

    }

    protected void newStringFields(final Consumer<Field> fieldConsumer, final String value, final FieldMetadata fieldMetadata) {
        final Field out = new StringField(fieldMetadata.name(), value, fieldMetadata.store());
        applyRemainingProperties(out, fieldMetadata);
        fieldConsumer.accept(out);
    }

    protected void newTextOrStringField(final Consumer<Field> fieldConsumer, final Character value, final FieldMetadata fieldMetadata) {
        newTextOrStringField(fieldConsumer, new String(new char[]{value.charValue()}), fieldMetadata);
    }

    protected void newTextOrStringField(final Consumer<Field> fieldConsumer, final CharSequence value, final FieldMetadata fieldMetadata) {

        final Field out;

        if (fieldMetadata.text()) {
            out = new TextField(fieldMetadata.name(), value.toString(), fieldMetadata.store());
        } else {
            out = new StringField(fieldMetadata.name(), value.toString(), fieldMetadata.store());
        }

        applyRemainingProperties(out, fieldMetadata);
        fieldConsumer.accept(out);

    }

    private void applyRemainingProperties(final Field field, final FieldMetadata searchableField) {

        if (searchableField.boost() != SearchableField.DEFAULT_BOOST) {
            // setBoost can result in an IllegalArgumentException, so this prevents that from
            // happening if the boost is left as the default value.
//            field.setBoost(searchableField.boost());

        }

    }

}
