package dev.getelements.elements.rt.protocol;

import dev.getelements.elements.rt.Event;
import dev.getelements.elements.rt.Request;
import dev.getelements.elements.rt.Response;

/**
 * An enumeration of message types.
 *
 * Created by patricktwohig on 9/11/15.
 */
public enum EnvelopeType {

    /**
     * Indicates that the following binary data is of type {@link Request}
     */
    REQUEST(1),

    /**
     * Indicates that the following binary data is for a {@link Response}
     */
    RESPONSE(2),

    /**
     * Indicates that the following binary data is for a {@link Event}
     */
    EVENT(3);

    private final int code;

    EnvelopeType(final int code) {
        this.code = code;
    }

    /**
     * Gets the code of the enveolope type.
     *
     * @return the code for the type.
     */
    public byte getCode() {
        return (byte)code;
    }

    public static EnvelopeType get(final byte code) {
        switch (code) {
            case 1:  return REQUEST;
            case 2:  return RESPONSE;
            case 3:  return EVENT;
            default: throw new IllegalArgumentException("Invalid/unknown envelope type " + code);
        }
    }

}
