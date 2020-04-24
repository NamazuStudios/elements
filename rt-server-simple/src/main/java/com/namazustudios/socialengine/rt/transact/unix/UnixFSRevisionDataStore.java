package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.transact.*;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.nio.file.Path;

public class UnixFSRevisionDataStore implements RevisionDataStore {

    public static final String STORAGE_ROOT_DIRECTORY = "com.namazustudios.socialengine.rt.transact.unix.fs.root";

    private static final String LOCK_FILE_NAME = "index.lock";

    private final Path root;

    private final Path lockFilePath;

    private final UnixFSUtils utils;

    private final UnixFSLinkPathIndex pathIndex;

    private final UnixFSResourceIdIndex resourceIdIndex;

    private final UnixFSReversePathIndex reversePathIndex;

    @Inject
    public UnixFSRevisionDataStore(
            final UnixFSUtils utils,
            final UnixFSLinkPathIndex pathIndex,
            final UnixFSReversePathIndex reversePathIndex,
            final UnixFSResourceIdIndex resourceIdIndex,
            @Named(STORAGE_ROOT_DIRECTORY) final Path storageRoot) throws IOException {
        this.utils = utils;
        this.root = storageRoot;
        this.pathIndex = pathIndex;
        this.resourceIdIndex = resourceIdIndex;
        this.reversePathIndex = reversePathIndex;
        lockFilePath = utils.lockDirectory(storageRoot);
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
    public ReversePathIndex getReversePathIndex() {
        return reversePathIndex;
    }

    @Override
    public ResourceIdIndex getResourceIdIndex() {
        return resourceIdIndex;
    }

    @Override
    public void apply(final TransactionJournal.MutableEntry entry) {

    }

}
