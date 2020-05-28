package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.transact.FatalException;
import com.namazustudios.socialengine.rt.transact.Revision;

import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Implements the garbage collection for the {@link UnixFSTransactionalResourceServicePersistence}.
 */
public class UnixFSGarbageCollector {

    private final UnixFSUtils utils;

    private final SortedMap<Revision<?>, Integer> locked = new ConcurrentSkipListMap<>();

    @Inject
    public UnixFSGarbageCollector(final UnixFSUtils utils) {
        this.utils = utils;
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

        if (!locked.containsKey(revision)) throw new IllegalStateException("Revision not locked by garbage collector.");
        if (!Files.isRegularFile(file)) throw new IllegalArgumentException("Not a file path: " + file);

        final Path root = file.getParent();
        final Path pinned = root.resolve(revision.getUniqueIdentifier());
        utils.doOperationV(() -> Files.createLink(file, pinned), FatalException::new);

    }

}
