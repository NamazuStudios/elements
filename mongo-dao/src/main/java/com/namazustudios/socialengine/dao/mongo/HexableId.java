package com.namazustudios.socialengine.dao.mongo;

/**
 * Interface to a Hex-able ID. The Hexable Id can be converted to a hexadeximal string and back. Types implementing
 * this interface must include a single argument constructor accepting a string which will be used to construct the
 * instance from the original hexadecimal string.
 */
public interface HexableId {

    /**
     * Converts the object to a Hex string.
     *
     * @return the hex string representation.
     */
    String toHexString();

}
