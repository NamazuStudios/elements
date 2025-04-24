package dev.getelements.elements.rt.transact.unix;

import dev.getelements.elements.rt.exception.ResourceNotFoundException;
import dev.getelements.elements.sdk.cluster.id.ResourceId;
import dev.getelements.elements.rt.transact.NullResourceException;
import dev.getelements.elements.rt.transact.ResourceContents;
import dev.getelements.elements.sdk.util.FinallyAction;
import dev.getelements.elements.sdk.util.LazyValue;
import dev.getelements.elements.sdk.util.SimpleLazyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import static dev.getelements.elements.sdk.cluster.path.Paths.WILDCARD_FIRST;
import static dev.getelements.elements.rt.transact.unix.UnixFSDataHeader.*;
import static java.lang.Math.min;
import static java.nio.ByteBuffer.allocate;
import static java.nio.channels.FileChannel.open;
import static java.nio.file.Files.isRegularFile;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardOpenOption.READ;
import static java.util.Collections.unmodifiableSet;

public class UnixFSResourceEntryExisting extends UnixFSResourceEntryBase {

    private static final int DEFAULT_PATH_BUFFER_SIZE = 4096;

    private static final Logger logger = LoggerFactory.getLogger(UnixFSResourceEntryExisting.class);

    private FinallyAction onClose = FinallyAction.begin(logger);

    private final Path reversePathsFilesystemPath;

    private final LazyValue<UnixFSDataHeader> reversePathsHeader = new SimpleLazyValue<>(this::loadReversePathsHeader);

    private UnixFSDataHeader loadReversePathsHeader() {
        return getUnixFSUtils().doOperation(() -> loadHeader(
                reversePathsFileChannel.get().position(0),
                REVERSE_PATH_MAGIC
        ));
    }

    private final LazyValue<Optional<FileChannel>> resourceContentsFileChannel = new SimpleLazyValue<>(this::openResourceContents);

    private Optional<FileChannel> openResourceContents() {

        final var resourceId = reversePathsHeader.get().resourceId.get();
        final var mapping = UnixFSResourceContentsMapping.fromResourceId(getUnixFSUtils(), resourceId);

        if (!isRegularFile(mapping.getFilesystemPath(), NOFOLLOW_LINKS)) {
            return Optional.empty();
        }

        return getUnixFSUtils().doOperation(() -> {
            final var fileChannel = open(mapping.getFilesystemPath(), READ);
            onClose = onClose.then(() -> getUnixFSUtils().doOperationV(fileChannel::close));
            return Optional.of(fileChannel);
        });

    }

    private final LazyValue<UnixFSDataHeader> resourceContentsHeader = new SimpleLazyValue<>(this::loadResourceContentsHeader);

    private UnixFSDataHeader loadResourceContentsHeader() {

        final var fileChannel = resourceContentsFileChannel
                .get()
                .orElseThrow(NullResourceException::new);

        return getUnixFSUtils().doOperation(() -> loadHeader(
                fileChannel,
                RESOURCE_CONTENTS_MAGIC
        ));

    }

    private final LazyValue<FileChannel> reversePathsFileChannel = new SimpleLazyValue<>(this::openReversePaths);

    private FileChannel openReversePaths() {
        return getUnixFSUtils().doOperation(() -> {
            final var fileChannel =  open(reversePathsFilesystemPath, READ);
            onClose = onClose.then(() -> getUnixFSUtils().doOperationV(fileChannel::close));
            return fileChannel;
        });
    }

    private final LazyValue<Set<dev.getelements.elements.sdk.cluster.path.Path>> reversePathsImmutable = new SimpleLazyValue<>(this::loadReversePathsImmutable);

    private Set<dev.getelements.elements.sdk.cluster.path.Path> loadReversePathsImmutable() {
        return getUnixFSUtils().doOperation(() -> {

            final var header = reversePathsHeader.get();
            final var treeSet = new TreeSet<>(WILDCARD_FIRST);
            final var fileChannel = reversePathsFileChannel.get().position(header.size());

            if (!reversePathsValid.get()) {
                throw new UnixFSChecksumFailureException("Invalid checksum for reverse paths:" + header.resourceId.get());
            }

            var read = 0;
            var readBuffer = allocate(DEFAULT_PATH_BUFFER_SIZE);

            fileChannel.position(header.size());

            while (fileChannel.position() < fileChannel.size()) {

                readBuffer.position(0).limit(Integer.BYTES);

                while (readBuffer.hasRemaining()) {

                    read = fileChannel.read(readBuffer);

                    if (read < 0) {
                        throw new IOException("Unexpected end of stream.");
                    }

                }

                final int pathLengthInBytes = readBuffer.flip().getInt();

                readBuffer = readBuffer.capacity() >= pathLengthInBytes
                        ? readBuffer
                        : allocate(pathLengthInBytes);

                readBuffer.clear().limit(pathLengthInBytes);

                while (readBuffer.hasRemaining()) {

                    read = fileChannel.read(readBuffer);

                    if (read < 0) {
                        throw new IOException("Unexpected end of stream.");
                    }

                }

                final var path = dev.getelements.elements.sdk.cluster.path.Path.fromByteBuffer(readBuffer.flip());
                treeSet.add(path);

            }

            return unmodifiableSet(treeSet);

        });
    }

    private final LazyValue<Boolean> contentsValid = new SimpleLazyValue<>(this::checkData);

    private boolean checkData() {

        final var header = this.resourceContentsHeader.get();

        final var fileChannel = resourceContentsFileChannel
                .get()
                .orElseThrow(NullResourceException::new);

        final var algo = header.checksumAlgorithm.get();

        return getUnixFSUtils().doOperation(() -> algo.isValid(
                header.checksum.get(),
                () -> fileChannel.position(header.size())
        ));

    }

    private final LazyValue<Boolean> reversePathsValid = new SimpleLazyValue<>(this::checkReversePaths);

    private Boolean checkReversePaths() {

        final var header = reversePathsHeader.get();
        final var algo = header.checksumAlgorithm.get();

        return getUnixFSUtils().doOperation(() -> algo.isValid(
                header.checksum.get(),
                () -> reversePathsFileChannel.get().position(header.size())
        ));

    }

    public UnixFSResourceEntryExisting(
            final UnixFSUtils unixFSUtils,
            final UnixFSHasFilesystemPath hasFilesystemPath,
            final OperationalStrategy operationalStrategy) throws IOException {

        super(operationalStrategy, unixFSUtils);

        this.reversePathsFilesystemPath = hasFilesystemPath.getFilesystemPath();

        if (!unixFSUtils.isRegularFile(hasFilesystemPath)) {
            throw new ResourceNotFoundException("No resource at path: " + reversePathsFilesystemPath);
        }

    }

    @Override
    public Optional<ResourceId> findOriginalResourceId() {
        return Optional.of(reversePathsHeader.get().resourceId.get());
    }

    @Override
    public Set<dev.getelements.elements.sdk.cluster.path.Path> getOriginalReversePathsImmutable() {
        return reversePathsImmutable.get();
    }

    @Override
    public Optional<ResourceContents> findOriginalResourceContents() {
        return Optional.of(() -> {

            if (!contentsValid.get()) {
                throw new UnixFSChecksumFailureException("Invalid data for resource: " + getOriginalResourceId());
            }

            return new ReadableByteChannel() {

                private boolean open = true;

                private int position = resourceContentsHeader.get().size();

                private final FileChannel fileChannel = resourceContentsFileChannel
                        .get()
                        .orElseThrow(NullResourceException::new);

                @Override
                public int read(final ByteBuffer dst) throws IOException {
                    if (!open) throw new ClosedChannelException();
                    final int read = fileChannel.position(position).read(dst);
                    position += min(read, 0);
                    return read;
                }

                @Override
                public boolean isOpen() {
                    return open && fileChannel.isOpen();
                }

                @Override
                public void close() {
                    open = false;
                }

            };

        });

    }

    @Override
    public boolean isOriginalReversePaths() {
        return super.isOriginalReversePaths() && reversePathsValid.getOptional().isPresent();
    }

    @Override
    public void close() {
        onClose.close();
    }

}
