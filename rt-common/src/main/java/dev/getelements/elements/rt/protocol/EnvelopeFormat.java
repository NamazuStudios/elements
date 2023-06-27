package dev.getelements.elements.rt.protocol;

/**
 * Indicates the format of the envelope.  Currently only BSON is supported.
 *
 * Created by patricktwohig on 9/29/15.
 */
public enum EnvelopeFormat {

    /**
     * Indicator that the evelope format is of BSON type.
     */
    BSON(1);

    private final int code;

    EnvelopeFormat(final int code) {
        this.code = code;
    }

    /**
     * Gets the code of the envelope format.
     *
     * @return the code
     */
    public byte getCode() {
        return (byte)code;
    }

    public static EnvelopeFormat get(final byte code) {
        switch (code) {
            case 1:  return BSON;
            default: throw new IllegalArgumentException("Invalid/unknown envelope format " + code);
        }
    }

}
