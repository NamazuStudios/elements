package com.namazustudios.socialengine.rt.transact.unix;

import javolution.io.Struct;

/**
 * File header for all objects in the RT Storage system.
 */
public class UnixFSObjectHeader extends Struct {

    public static final int VERSION_MAJOR_1 = 1;

    public static final int VERSION_MINOR_0 = 0;

    public static final int VERSION_MAJOR_CURRENT = VERSION_MAJOR_1;

    public static final int VERSION_MINOR_CURRENT = VERSION_MINOR_0;

    /**
     * Indicates the major version of the file
     */
    public Unsigned32 majorVersion = new Unsigned32();

    /**
     * Indicates the minor version of the file.
     */
    public Unsigned32 minorVersion = new Unsigned32();

    public Enum8<UnixFSChecksumAlgorithm> algorithm = new Enum8<>(UnixFSChecksumAlgorithm.values());

    /**
     * The CRC-32 for the file
     */
    public Unsigned32 checksum = new Unsigned32();

    public UnixFSObjectHeader() {
        checksum.set(0);
        majorVersion.set(VERSION_MAJOR_CURRENT);
        minorVersion.set(VERSION_MINOR_CURRENT);
    }

}
