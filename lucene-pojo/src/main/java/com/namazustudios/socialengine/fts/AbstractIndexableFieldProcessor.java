package com.namazustudios.socialengine.fts;

import com.namazustudios.socialengine.fts.annotation.SearchableField;
import org.apache.lucene.document.*;

/**
 * Created by patricktwohig on 6/1/15.
 */
public abstract class AbstractIndexableFieldProcessor<FieldT> implements IndexableFieldProcessor<FieldT> {

    protected Field newStoredField(final byte[] value, final FieldMetadata fieldMetadata) {
        final Field out = new StoredField(fieldMetadata.name(), value);
        applyRemainingProperties(out, fieldMetadata);
        return out;
    }

    protected Field newIntegerField(final Number value, final FieldMetadata fieldMetadata) {
        final Field out = new IntField(fieldMetadata.name(), value.intValue(), fieldMetadata.store());
        applyRemainingProperties(out, fieldMetadata);
        return out;
    }

    protected Field newLongField(final Number value, final FieldMetadata fieldMetadata) {
        final Field out = new LongField(fieldMetadata.name(), value.longValue(), fieldMetadata.store());
        applyRemainingProperties(out, fieldMetadata);
        return out;
    }

    protected Field newFloatField(final Number value, final FieldMetadata fieldMetadata) {
        final Field out = new FloatField(fieldMetadata.name(), value.floatValue(), fieldMetadata.store());
        applyRemainingProperties(out, fieldMetadata);
        return out;
    }

    protected Field newDoubleField(final Number value, final FieldMetadata fieldMetadata) {
        final Field out = new DoubleField(fieldMetadata.name(), value.doubleValue(), fieldMetadata.store());
        applyRemainingProperties(out, fieldMetadata);
        return out;
    }

    protected Field newStringField(final String value, final FieldMetadata fieldMetadata) {
        final Field out = new StringField(fieldMetadata.name(), value, fieldMetadata.store());
        applyRemainingProperties(out, fieldMetadata);
        return out;
    }

    protected Field newTextOrStringField(final Character value, final FieldMetadata fieldMetadata) {
        return newTextOrStringField(new String(new char[]{value.charValue()}), fieldMetadata);
    }

    protected Field newTextOrStringField(final CharSequence value, final FieldMetadata fieldMetadata) {

        final Field out;

        if (fieldMetadata.text()) {
            out = new TextField(fieldMetadata.name(), value.toString(), fieldMetadata.store());
        } else {
            out = new StringField(fieldMetadata.name(), value.toString(), fieldMetadata.store());
        }

        applyRemainingProperties(out, fieldMetadata);
        return out;

    }

    private void applyRemainingProperties(final Field field, final FieldMetadata searchableField) {

        if (searchableField.boost() != SearchableField.DEFAULT_BOOST) {
            // setBoost can result in an IllegalArgumentException, so this prevents that from
            // happening if the boost is left as the default value.
            field.setBoost(searchableField.boost());
        }

    }

}
