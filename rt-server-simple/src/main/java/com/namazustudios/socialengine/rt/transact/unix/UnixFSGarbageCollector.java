package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.transact.DefaultTransactionalResourceServicePersistence;
import com.namazustudios.socialengine.rt.transact.FatalException;
import com.namazustudios.socialengine.rt.transact.Revision;

import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.String.format;
import static java.nio.file.Files.isRegularFile;
import static java.nio.file.Files.isSymbolicLink;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

/**
 * Implements the garbage collection for the {@link DefaultTransactionalResourceServicePersistence}.
 */
public class UnixFSGarbageCollector {

    final UnixFSUtils utils;

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
     * @return the {@link Path} to the pinned file.
     */
    public Path pin(final Path file, final Revision<?> revision) {
        if (isRegularFile(file, NOFOLLOW_LINKS) || isSymbolicLink(file)) {
            final Path parent = file.getParent();
            final Path pinned = utils.resolveRevisionFilePath(parent, revision);
            utils.doOperationV(() -> Files.createLink(file, pinned), FatalException::new);
            return pinned;
        } else {
            throw new IllegalArgumentException("Not a file path: " + file);
        }
    }

    /**
     * Hints that the following {@link Revision<?>} may be eligible for garbage collection.
     *
     * @param revision
     */
    public void hint(final UnixFSRevision<?> revision) {
        // TODO Implement
    }

    public void start() {
        // TODO Implement
    }

    public void stop() {
        // TODO Implement
    }

}
