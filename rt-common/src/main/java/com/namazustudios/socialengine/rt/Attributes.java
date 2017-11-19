package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.exception.InvalidConversionException;
import org.omg.CORBA.DynAnyPackage.Invalid;

import java.util.List;
import java.util.Set;

/**
 * Contains attributes which may be attached to a {@link Request} or similar object.  These are transient properties
 * which live in memory and are associated temporarily with the associated object.  These do no persist between requests
 * and are used to set various context objects to the associated type.
 *
 * Attributes may be set by the underlying container to provide context or metadata to the associated object.
 */
public interface Attributes {

    /**
     * Gets a {@link List<String>} containing all attribute names contained in this instance.
     *
     * @return the {@link List<String>} of attribute names
     */
    List<String> getAttributeNames();

    /**
     * Gets the attribute associated with this {@link Attributes} object.
     *
     * @param name the attribute name
     * @return the attribute value, or null
     */
    Object getAttribute(String name);

    /**
     * Gets the attribute with the supplied name.  If the attribute does not exist, then this returns the specified
     * default value.
     *
     * @param name the name
     * @param defaultValue the default value
     * @return the {@link Object} representing the attribute or the default value
     */
    default Object getAttributeOrDefault(final String name, final Object defaultValue) {
        return getAttributeNames().contains(name) ? getAttribute(name) : defaultValue;
    }

    /**
     * Gets the attribute with the supplied name and {@link Class<T>}.  This attempts to convert the attribute value
     * to the specified type.  The default implementation of this method simply attempts to cast, but subclasses may
     * provide a more refined means to convert the attribute value.
     *
     * @param name the name of the attribute
     * @param tClass the desired type
     * @param <T> the desired type
     * @return the attribvute value or null
     * @throws {@link InvalidConversionException} if the conversion fails
     */
    default <T> T getAndConvertAttribute(final String name, final Class<T> tClass) throws InvalidConversionException {

        final Object attributeValue = getAttribute(name);

        try {
            return tClass.cast(attributeValue);
        } catch (ClassCastException ex) {
            throw new InvalidConversionException(ex);
        }

    }

}
