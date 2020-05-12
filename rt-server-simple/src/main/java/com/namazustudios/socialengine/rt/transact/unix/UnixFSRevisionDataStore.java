package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.transact.*;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.nio.file.Path;

public class UnixFSRevisionDataStore implements RevisionDataStore {

    public static final String STORAGE_ROOT_DIRECTORY = "com.namazustudios.socialengine.rt.transact.unix.fs.root";

    private final Path lockFilePath;

    private final UnixFSUtils utils;

    private final UnixFSPathIndex pathIndex;

    private final UnixFSResourceIndex resourceIdIndex;

    @Inject
    public UnixFSRevisionDataStore(
            final UnixFSUtils utils,
            final UnixFSPathIndex pathIndex,
            final UnixFSResourceIndex resourceIdIndex,
            @Named(STORAGE_ROOT_DIRECTORY) final Path storageRoot) throws IOException {
        this.utils = utils;
        this.pathIndex = pathIndex;
        this.resourceIdIndex = resourceIdIndex;
        this.lockFilePath = utils.lockPath(storageRoot);
    }

    @Override
    public void close() {
        this.utils.unlockDirectory(lockFilePath);
    }

    @Override
    public PathIndex getPathIndex() {
        return pathIndex;
    }

    @Override
    public ResourceIndex getResourceIndex() {
        return resourceIdIndex;
    }

    @Override
    public void apply(final TransactionJournal.MutableEntry entry) {

    }

}
