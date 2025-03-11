package dev.getelements.elements.rt.transact.unix;

import dev.getelements.elements.sdk.cluster.id.ResourceId;
import dev.getelements.elements.rt.transact.ResourceContents;
import dev.getelements.elements.rt.transact.ResourceEntry;
import dev.getelements.elements.rt.transact.ResourceIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import java.util.Optional;
import java.util.function.Supplier;

import static java.nio.file.Files.isRegularFile;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

public class UnixFSResourceIndex implements ResourceIndex {

    private static final Logger logger = LoggerFactory.getLogger(UnixFSResourceIndex.class);

    private UnixFSUtils utils;

    @Override
    public void cleanup(final ResourceId resourceId, final String transactionId) {
        final var mapping = UnixFSResourceContentsMapping.fromResourceId(utils, resourceId);
        getUtils().cleanup(mapping, transactionId);
    }

    @Override
    public ResourceContents updateContents(final ResourceId resourceId) {
        final var mapping = UnixFSResourceContentsMapping.fromResourceId(getUtils(), resourceId);
        return new UnixFSTemporaryResourceContents(mapping);
    }

    @Override
    public void applyReversePathsChange(final ResourceId resourceId,
                                        final String transactionId) {
        final var mapping = UnixFSReversePathMapping.fromResourceId(getUtils(), resourceId);
        getUtils().commit(mapping, transactionId);
    }

    @Override
    public void applyContentsChange(final ResourceId resourceId,
                                    final String transactionId) {
        final var mapping = UnixFSResourceContentsMapping.fromResourceId(getUtils(), resourceId);
        getUtils().commit(mapping, transactionId);
    }

    @Override
    public ResourceEntry newEntry(final ResourceId resourceId,
                                  final Supplier<ResourceEntry.OperationalStrategy> operationalStrategy) {
        return getUtils().doOperation(() -> new UnixFSResourceEntryNew(
                getUtils(),
                resourceId,
                operationalStrategy.get()
        ));
    }

    @Override
    public Optional<ResourceEntry> findEntry(
            final ResourceId resourceId,
            final Supplier<ResourceEntry.OperationalStrategy> operationalStrategy) {

        final var mapping = UnixFSReversePathMapping.fromResourceId(getUtils(), resourceId);

        if (!isRegularFile(mapping.getFilesystemPath(), NOFOLLOW_LINKS)) {
            return Optional.empty();
        }

        final var entry = getUtils().doOperation(() -> new UnixFSResourceEntryExisting(
                getUtils(),
                mapping,
                operationalStrategy.get()
        ));

        return Optional.of(entry);

    }

    public UnixFSUtils getUtils() {
        return utils;
    }

    @Inject
    public void setUtils(UnixFSUtils utils) {
        this.utils = utils;
    }

}
