package dev.getelements.elements.rt.transact.unix;

import dev.getelements.elements.sdk.cluster.path.Path;
import dev.getelements.elements.rt.transact.PathIndex;
import dev.getelements.elements.rt.transact.ResourceEntry;

import java.io.IOException;
import java.io.UncheckedIOException;

class UnixFSPathListing implements PathIndex.Listing {

    private final UnixFSPathMapping mapping;

    public UnixFSPathListing(final UnixFSPathMapping mapping) {
        this.mapping = mapping;
    }

    @Override
    public Path getPath() {
        return mapping.getPath();
    }

    @Override
    public ResourceEntry open(final ResourceEntry.OperationalStrategy operationalStrategy) {
        try {
            return new UnixFSResourceEntryExisting(mapping.getUnixFSUtils(), mapping, operationalStrategy);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

}
