package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.exception.InvalidConversionException;

import java.io.Serializable;

/**
 * Created by patricktwohig on 7/27/15.
 */
public interface Response {

    /**
     * Gets the response header.
     *
     * @return the response header.
     */
    ResponseHeader getResponseHeader();

    /**
     * Gets the payload of the response.
     */
    Object getPayload();

    /**
     * Gets the payload, converted to the given type.  The default implementation attempts a cast and will throw an
     * instance of {@link InvalidConversionException} if the conversion fails.
     *
     * Subclasses may opt to provide more robust implementations and in the event of conversion failure, an instance of
     * {@link InvalidConversionException} must be thrown to indicate the error condition.
     *
     * @param cls the type
     * @param <T> the type
     */
    default <T> T getPayload(final Class<T> cls) {

        final Object payload = getPayload();

        try {
            return cls.cast(payload);
        } catch (ClassCastException ex) {
            throw new InvalidConversionException(ex);
        }

    }

}
