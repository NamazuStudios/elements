package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.transact.FatalException;
import com.namazustudios.socialengine.rt.transact.Revision;

import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;

import static java.lang.String.format;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

/**
 * Implements the garbage collection for the {@link UnixFSTransactionalResourceServicePersistence}.
 */
public class UnixFSGarbageCollector {

    private final UnixFSUtils utils;

    private final String TOMBSTONE_FILE = "tombstone";

    private final SortedMap<Revision<?>, Integer> locked = new ConcurrentSkipListMap<>();

    @Inject
    public UnixFSGarbageCollector(final UnixFSUtils utils) {
        this.utils = utils;
    }

    /**
     * Flags the supplied directory for deletion.
     * @param directory
     */
    public void tombstone(final Path directory, Revision<?> revision) {

        if (Files.isDirectory(directory, NOFOLLOW_LINKS))
            throw new IllegalArgumentException(directory + " is not a directory.");

        // TODO: Tombstone a directory at a particular revision.

    }

    /**
     * Pins the particular file to the supplied {@link Revision<?>}.  This will guarantee that the file pointed to the
     * {@link Path} will be preserved until the supplied {@link Revision<?>} is released.  This only works if the
     * garbage collector has the {@link Revision} locked.
     *
     * @param file the revision file to pin
     * @param revision the {@link Revision<?>}
     * @return the {@link Path} to the pinned file.
     */
    public Path pin(final Path file, final Revision<?> revision) {

        if (!locked.containsKey(revision)) throw new IllegalStateException("Revision not locked by garbage collector.");
        if (!Files.isRegularFile(file)) throw new IllegalArgumentException("Not a file path: " + file);

        final Path parent = file.getParent();
        final Path pinned = utils.resolveRevisionPath(parent, revision);
        utils.doOperationV(() -> Files.createLink(file, pinned), FatalException::new);

        return pinned;
    }

    public Path pinLatest(final Path directory, final Revision<?> revision) {

        final Revision<Path> file = utils.findLatestForRevision(directory, revision);

        if (file.getValue().isPresent()) {
            return pin(file.getValue().get(), revision);
        } else {
            throw new FatalException(format("No latest revision exists for %s@%s", directory, revision));
        }

    }

}
