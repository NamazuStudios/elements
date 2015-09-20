package com.namazustudios.socialengine.rt.mina;

/**
 * Created by patricktwohig on 9/11/15.
 */
public enum BSONMessageType {

    RESPONSE((byte)1),

    EVENT((byte)2);

    private final byte code;

    BSONMessageType(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return code;
    }

    public static BSONMessageType get(final byte code) {
        switch (code) {
        case 1:
            return RESPONSE;
        case 2:
            return EVENT;
        default:
            throw new IllegalArgumentException("Invalid message type " + code);
        }
    }

}
