package dev.getelements.elements.rt.transact.unix;

import com.google.common.collect.Streams;
import dev.getelements.elements.sdk.cluster.path.Path;
import dev.getelements.elements.rt.exception.InternalException;
import dev.getelements.elements.rt.transact.PathIndex;
import dev.getelements.elements.rt.transact.ResourceEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static dev.getelements.elements.rt.transact.unix.UnixFSPathMapping.fromFullyQualifiedFSPath;
import static dev.getelements.elements.rt.transact.unix.UnixFSUtils.CommitResult.DELETED;
import static java.nio.file.Files.exists;

public class UnixFSPathIndex implements PathIndex {

    private static final Logger logger = LoggerFactory.getLogger(UnixFSPathIndex.class);

    private UnixFSUtils unixFSUtils;

    @Override
    public void cleanup(final Path path, final String transactionId) {
        final var mapping = UnixFSPathMapping.fromRTPath(getUnixFSUtils(), path);
        getUnixFSUtils().cleanup(mapping, transactionId);
    }

    @Override
    public void applyChange(final Path path, final String transactionId) {

        final var mapping = UnixFSPathMapping.fromRTPath(getUnixFSUtils(), path);

        if (DELETED.equals(getUnixFSUtils().commit(mapping, transactionId))) {
            final var directory = getUnixFSUtils().stripExtension(mapping.getFilesystemPath());
            getUnixFSUtils().prune(directory);
        }

    }

    @Override
    public Optional<ResourceEntry> findEntry(
            final Path path,
            final Supplier<ResourceEntry.OperationalStrategy> operationalStrategy) {

        final var mapping = UnixFSPathMapping.fromRTPath(getUnixFSUtils(), path);

        if (!getUnixFSUtils().isRegularFile(mapping)) {
            return Optional.empty();
        }

        try {
            final var entry = new UnixFSResourceEntryExisting(getUnixFSUtils(), mapping, operationalStrategy.get());
            return Optional.of(entry);
        } catch (FileNotFoundException ex) {
            return Optional.empty();
        } catch (IOException ex) {
            throw new InternalException(ex);
        }

    }

    @Override
    public Stream<PathIndex.Listing> list(final Path rtPath) {
        return rtPath.isWildcard() || rtPath.isWildcardRecursive()
                ? wildcardListing(rtPath)
                : singularListing(rtPath);
    }

    private Stream<Listing> wildcardListing(final Path rtPath) {

        final var topMost = rtPath.stripWildcard(0);
        final var mapping = UnixFSPathMapping.fromRTPath(getUnixFSUtils(), topMost);

        return getUnixFSUtils().doOperation(() -> {

            final Stream<java.nio.file.Path> pathStream = exists(mapping.getFilesystemPath())
                    ? Stream.of(mapping.getFilesystemPath())
                    : Stream.empty();

            final var stripped = getUnixFSUtils()
                    .stripExtension(mapping.getFilesystemPath());

            final Stream<java.nio.file.Path> listStream = Files.exists(stripped)
                    ? Files.list(stripped)
                    : Stream.empty();

            return Streams.concat(pathStream, listStream)
                    .filter(Files::isRegularFile)
                    .filter(p -> getUnixFSUtils().isMatchingExtension(p, UnixFSUtils.REVERSE_PATH_EXTENSION))
                    .map(fsPath -> fromFullyQualifiedFSPath(getUnixFSUtils(), rtPath, fsPath))
                    .filter(m -> rtPath.matches(m.getPath()))
                    .map(UnixFSPathListing::new);

        });

    }

    private Stream<PathIndex.Listing> singularListing(final Path rtPath) {
        final var mapping = UnixFSPathMapping.fromRTPath(getUnixFSUtils(), rtPath);
        final var listing = new UnixFSPathListing(mapping);
        return Stream.of(listing);
    }

    public UnixFSUtils getUnixFSUtils() {
        return unixFSUtils;
    }

    @Inject
    public void setUnixFSUtils(UnixFSUtils unixFSUtils) {
        this.unixFSUtils = unixFSUtils;
    }

}
