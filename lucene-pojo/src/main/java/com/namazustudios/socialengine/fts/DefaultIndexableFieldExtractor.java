package com.namazustudios.socialengine.fts;

import com.namazustudios.socialengine.fts.annotation.SearchableDocument;
import com.namazustudios.socialengine.fts.annotation.SearchableField;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.util.BytesRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 *
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
 *  <li>Class<?> - attempts to load using {@link Class#forName(String)}</?></li>
 *  <li>Enum<?> - attempts to find the enumerant using {@link Enum#valueOf(Class, String)}</li>
 *  <li>{@link CharSequence} - passed directly through</li>
 * </ul>
 *
 * <p>
 * <em>Caveat:</em>  because of Java's type erasure, this has no means to handle any
 * {@link Iterable} types.
 * </p>
 *
 * <p>
 * <em>Caveat:</em>  because {@link SearchableDocument} can be processed for the entire class hierarchy,
 * multiple classes may be present.  Therefore, this will always find the class lowest in the hierarchy.
 * </p>
 *
 * Created by patricktwohig on 5/14/15.
 */
public class DefaultIndexableFieldExtractor implements IndexableFieldExtractor<Object> {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultIndexableFieldExtractor.class);

    @Override
    public Object extract(final Document document, final FieldMetadata fieldMetadata) {


        final Class<?> type = fieldMetadata.type();

        // The byte array type, which is just copied back out directly.

        if (type.isAssignableFrom(byte[].class)) {

            final BytesRef bytesRef = document.getBinaryValue(fieldMetadata.name());

            if (bytesRef == null) {
                throw new FieldExtractionException(fieldMetadata, document, "no binary value for field " + fieldMetadata);
            }

            return bytesRef.bytes.clone();

        }

        // All remaining types can be parsed from the string.

        try {

            final String stringValue = document.get(fieldMetadata.name());

            if (stringValue == null) {
                throw new FieldExtractionException(fieldMetadata, document, "no string value for field " + fieldMetadata);
            }

            if (Byte.class.isAssignableFrom(type) || byte.class.isAssignableFrom(type)) {
                return new Byte(stringValue);
            } else if (Short.class.isAssignableFrom(type) || short.class.isAssignableFrom(type)) {
                return new Short(stringValue);
            } else if (Integer.class.isAssignableFrom(type) || int.class.isAssignableFrom(type)) {
                return new Integer(stringValue);
            } else if (Long.class.isAssignableFrom(type) || long.class.isAssignableFrom(type)) {
                return new Long(stringValue);
            } else if (Float.class.isAssignableFrom(type) || float.class.isAssignableFrom(type)) {
                return new Float(stringValue);
            } else if (Double.class.isAssignableFrom(type) || double.class.isAssignableFrom(type)) {
                return new Double(stringValue);
            } else if (char[].class.isAssignableFrom(type)) {
                return stringValue.toCharArray();
            } else if (CharSequence.class.isAssignableFrom(type)) {
                return stringValue;
            } else if (Enum.class.isAssignableFrom(type)) {
                return Enum.valueOf((Class<? extends Enum>)type, stringValue);
            } else if (Character.class.isAssignableFrom(type) || char.class.isAssignableFrom(type)) {

                if (stringValue.length() > 1) {
                    throw new FieldExtractionException(fieldMetadata, document,
                        "string length >1 for field " + fieldMetadata);
                }

                return stringValue.isEmpty() ? Character.valueOf((char)0) : stringValue.charAt(0);

            } else if (Class.class.isAssignableFrom(type)) {
                return Class.forName(stringValue);
            }

        } catch (NumberFormatException nfe) {
            throw new FieldExtractionException(fieldMetadata, document, nfe);
        } catch (ClassNotFoundException ex) {
            throw new FieldExtractionException(fieldMetadata, document, ex);
        }

        // If we get to this point, then we simply just throw the exception.  We're simply
        // not able to extract the field.

        throw new FieldExtractionException(fieldMetadata, document, "unable to extract value for field " + fieldMetadata);

    }


}
