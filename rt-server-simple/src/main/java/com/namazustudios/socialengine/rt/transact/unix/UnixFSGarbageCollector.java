package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.transact.Revision;

import java.nio.file.Path;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Implements the garbage collection for the {@link UnixFSTransactionalResourceServicePersistence}.
 */
public class UnixFSGarbageCollector {

    private SortedSet<Revision<?>> locked = new TreeSet<>();

    /**
     * Locks the supplied {@link Revision<?>} with this garbage collector.  While locked, this
     * {@link UnixFSGarbageCollector} will not collect the supplied {@link Revision<?>}
     *
     * @param revision
     */
    public void lock(final Revision<?> revision) {

    }

    /**
     * Unlocks the supplied {@link Revision<?>}, making it potentially available for collection
     *
     * @param revision
     */
    public void unlock(final Revision<?> revision) {

    }

    /**
     * Pins the particular file to the supplied {@link Revision<?>}.  This will guarantee that the file pointed to the
     * {@link Path} will be preserved until the supplied {@link Revision<?>} is released.  This only works if the
     * garbage collector has the {@link Revision} locked.
     *
     * @param file the revision file to pin
     * @param revision the {@link Revision<?>}
     */
    public void pin(final Path file, final Revision<?> revision) {
        if (!locked.contains(revision)) throw new IllegalStateException("Revision not locked by garbage collector.");
        // TODO Implement This Method
    }

    public void unlinkTemporary(final Path temporaryFile) {
        // TODO Implement This Method
    }

}
