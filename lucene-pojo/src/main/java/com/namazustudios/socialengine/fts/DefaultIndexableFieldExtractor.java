package com.namazustudios.socialengine.fts;

import com.namazustudios.socialengine.fts.annotation.SearchableField;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.util.BytesRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extracts fields from a {@link Document} with the default rules.  This is similar to the
 * {@link DefaultIndexableFieldProcessor}.  This relies on the specification of the
 * {@link SearchableField#type()} field.  This gets the String value of each field and
 * and parses the result out.  The exception being the byte[] type, where the value
 * is just copied to a new byte array and returned.
 *
 * <ul>
 *  <li>byte - parsed from string using new {@link Byte#Byte(String)}</li>
 *  <li>char - read from the first character of the string (an exception is thrown if the string is longer than 1 character)</li>
 *  <li>short - parsed as short using new {@link Short#Short(String)}</li>
 *  <li>int - parsed as an integer using new {@link Integer#Integer(String)}</li>
 *  <li>long - parsed as a long using new {@link Long#Long(String)}</li>
 *  <li>float - parsed as a float using new {@link Float#Float(String)}</li>
 *  <li>double - parsed as a double using new {@link Double#Double(String)}</li>
 *  <li>String - passed directly through</li>
 *  <li>{@link CharSequence} - passed directly through</li>
 * </ul>
 *
 * <em>Caveat:</em>  because of Java's type erasure, this has no means to handle any
 * {@link Iterable} types.
 *
 * Created by patricktwohig on 5/14/15.
 */
public class DefaultIndexableFieldExtractor implements IndexableFieldExtractor<Object> {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultIndexableFieldExtractor.class);

    @Override
    public Object extract(Document document, FieldMetadata fieldMetadata) {

        final Class<?> type = fieldMetadata.type();

        try {

            final String stringValue = document.get(fieldMetadata.name());

            if (stringValue == null) {
                throw new FieldExtractionException(fieldMetadata, document, "no string value for field " + fieldMetadata);
            }

            if (type.isAssignableFrom(Byte.class)) {
                return new Byte(stringValue);
            } else if (type.isAssignableFrom(Short.class)) {
                return new Short(stringValue);
            } else if (type.isAssignableFrom(Integer.class)) {
                return new Integer(stringValue);
            } else if (type.isAssignableFrom(Long.class)) {
                return new Long(stringValue);
            } else if (type.isAssignableFrom(Float.class)) {
                return new Float(stringValue);
            } else if (type.isAssignableFrom(Double.class)) {
                return new Double(stringValue);
            }

        } catch (NumberFormatException nfe) {
            throw new FieldExtractionException(fieldMetadata, document, nfe);
        }

        if (type.isAssignableFrom(byte[].class)) {

            final BytesRef bytesRef = document.getBinaryValue(fieldMetadata.name());

            if (bytesRef == null) {
                throw new FieldExtractionException(fieldMetadata, document, "no binary value for field " + fieldMetadata);
            }

            return bytesRef.bytes.clone();

        }

        final String stringValue = document.get(fieldMetadata.name());

        if (stringValue == null) {
            throw new FieldExtractionException(fieldMetadata, document, "no string value for field " + fieldMetadata);
        }

        if (type.isAssignableFrom(char[].class)) {
            return stringValue.toCharArray();
        } else if (type.isAssignableFrom(CharSequence.class)) {
            return stringValue;
        } else if (type.isAssignableFrom(Character.class)) {

            if (stringValue.length() > 1) {
                throw new FieldExtractionException(fieldMetadata, document, "string length >1 for field " + fieldMetadata);
            }

            return stringValue.isEmpty() ? Character.valueOf((char)0) : stringValue.charAt(0);

        }

        throw new FieldExtractionException(fieldMetadata, document, "unable to extract value for field " + fieldMetadata);

    }

}
