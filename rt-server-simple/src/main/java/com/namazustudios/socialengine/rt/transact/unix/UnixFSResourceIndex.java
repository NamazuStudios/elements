package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.transact.ResourceIndex;
import com.namazustudios.socialengine.rt.transact.Revision;
import com.namazustudios.socialengine.rt.transact.RevisionFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.util.Optional;

public class UnixFSResourceIndex implements ResourceIndex {

    private final NodeId nodeId;

    private final UnixFSUtils utils;

    private final RevisionFactory revisionFactory;

    private final UnixFSPathIndex unixFSPathIndex;

    private final UnixFSGarbageCollector garbageCollector;

    @Inject
    public UnixFSResourceIndex(
            final NodeId nodeId,
            final UnixFSUtils utils,
            final RevisionFactory revisionFactory,
            final UnixFSPathIndex unixFSPathIndex,
            final UnixFSGarbageCollector garbageCollector) throws IOException {
        this.nodeId = nodeId;
        this.utils = utils;
        this.revisionFactory = revisionFactory;
        this.unixFSPathIndex = unixFSPathIndex;
        this.garbageCollector = garbageCollector;
    }

    @Override
    public Revision<Boolean> existsAt(final Revision<?> revision, final ResourceId resourceId) {
        final Path resourceIdDirectory = utils.getResourceStorageRoot().resolve(resourceId.asString());
        return utils.getFileForRevision(resourceIdDirectory, revision).map(f -> true);
    }

    @Override
    public Revision<ReadableByteChannel> loadResourceContentsAt(
            final Revision<?> revision,
            final com.namazustudios.socialengine.rt.Path path) {

        final Optional<ReadableByteChannel> readableByteChannelOptional = unixFSPathIndex
            .loadRevisionListing(path, revision)
            .filter(rv -> !rv.isTombstone())
            .map(rv -> load(revision, rv.getResourceId()));

        return revisionFactory.createOptional(revision, readableByteChannelOptional);

    }

    @Override
    public Revision<ReadableByteChannel> loadResourceContentsAt(
            final Revision<?> revision,
            final ResourceId resourceId) {
        final Path resourceIdDirectory = utils.getResourceStorageRoot().resolve(resourceId.asString());
        return utils.getFileForRevision(resourceIdDirectory, revision).map(file -> load(file));
    }

    private ReadableByteChannel load(final Revision<?> revision, final ResourceId resourceId) {

        // TODO Load
        return null;
    }

    private ReadableByteChannel load(final Path file) {
        // TODO Load
        return null;
    }

}
