package com.namazustudios.socialengine.rt.transact.unix;

import javolution.io.Struct;

public class UnixFSHeader extends Struct {

    public static final int VERSION_MAJOR_1 = 1;

    public static final int VERSION_MINOR_0 = 0;

    public static final int VERSION_MAJOR_CURRENT = VERSION_MAJOR_1;

    public static final int VERSION_MINOR_CURRENT = VERSION_MINOR_0;

    /**
     * Indicates that the value has been flagged for deletion.
     */
    public Bool tombstone = new Bool();

    /**
     * Indicates the major version of the file
     */
    public Unsigned32 majorVersion = new Unsigned32();

    /**
     * Indicates the minor version of the file.
     */
    public Unsigned32 minorVersion = new Unsigned32();

    /**
     * The CRC-32 for the file.s
     */
    public Unsigned32 crc32 = new Unsigned32();

    public UnixFSHeader() {
        crc32.set(0);
        tombstone.set(false);
        majorVersion.set(VERSION_MAJOR_CURRENT);
        minorVersion.set(VERSION_MINOR_CURRENT);
    }

}
