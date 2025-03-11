package dev.getelements.elements.rt.transact.unix;

import dev.getelements.elements.sdk.cluster.path.Path;
import dev.getelements.elements.sdk.cluster.id.NodeId;
import dev.getelements.elements.sdk.cluster.id.ResourceId;
import dev.getelements.elements.rt.transact.ResourceContents;
import dev.getelements.elements.rt.transact.ResourceEntry;
import dev.getelements.elements.rt.transact.TransactionJournal;
import dev.getelements.elements.sdk.util.TemporaryFiles;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static dev.getelements.elements.sdk.cluster.id.NodeId.randomNodeId;
import static dev.getelements.elements.sdk.cluster.id.ResourceId.randomResourceIdForNode;
import static dev.getelements.elements.rt.transact.unix.UnixFSResourceContentsMapping.fromResourceId;
import static java.nio.ByteBuffer.allocate;
import static java.nio.file.Files.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

public class UnixFSResourceEntryTest {

    private static final TemporaryFiles temporaryFiles = new TemporaryFiles(UnixFSResourceEntryTest.class);

    @Factory
    public static Object[] getTests() {
        return Arrays
                .stream(UnixFSChecksumAlgorithm.values())
                .map(UnixFSResourceEntryTest::new)
                .toArray(Object[]::new);
    }

    private final UnixFSUtils utils;

    private final NodeId nodeId = randomNodeId();

    private final ResourceId resourceId = randomResourceIdForNode(nodeId);

    private final Set<Path> reverse = IntStream.range(0, 10)
            .mapToObj(i -> new Path("/test/*")
                    .toPathWithContext(nodeId.toString())
                    .appendUUIDIfWildcard())
            .collect(Collectors.toSet());

    public UnixFSResourceEntryTest(final UnixFSChecksumAlgorithm algorithm) {
        utils = new UnixFSUtils(algorithm, temporaryFiles.createTempDirectory());
        utils.initialize();
    }

    @Test
    public void testFlushReversePaths() throws IOException {

        final var journalEntry = mock(TransactionJournal.MutableEntry.class);
        when(journalEntry.getTransactionId()).thenReturn("test");

        final var strategy = new ResourceEntry.OperationalStrategy() {
            @Override
            public Set<Path> doGetReversePathsImmutable(final ResourceEntry entry) {
                return reverse;
            }

            @Override
            public boolean doIsOriginalReversePaths(ResourceEntry entry) {
                return false;
            }

        };

        try (final var entry = new UnixFSResourceEntryNew(utils, resourceId, strategy)) {
            entry.flush(journalEntry);
        }

        final var reverseMapping = UnixFSReversePathMapping.fromResourceId(utils, resourceId);

        // Checks all paths in the transaction are the same.

        for (var path : reverse) {

            final var pathMapping = UnixFSPathMapping.fromRTPath(utils, path);

            assertTrue(isSameFile(
                    pathMapping.getFilesystemPath(journalEntry.getTransactionId()),
                    reverseMapping.getFilesystemPath(journalEntry.getTransactionId()))
            );

            utils.commit(pathMapping, journalEntry.getTransactionId());

        }

        // Commits and checks all links are the same again.

        utils.commit(reverseMapping, journalEntry.getTransactionId());

        try (final var entry = new UnixFSResourceEntryExisting(
                utils,
                reverseMapping,
                new ResourceEntry.OperationalStrategy(){})) {
            final var actual = entry.getOriginalReversePathsImmutable();
            assertEquals(actual, reverse);
        }

        // Reads the file and ensures that the path mapping matches

        for (var path : reverse) {
            final var pathMapping = UnixFSPathMapping.fromRTPath(utils, path);
            assertTrue(isSameFile(
                    pathMapping.getFilesystemPath(),
                    reverseMapping.getFilesystemPath())
            );
        }

    }

    @Test
    public void testFlushContents() throws IOException {

        final var journalEntry = mock(TransactionJournal.MutableEntry.class);
        when(journalEntry.getTransactionId()).thenReturn("test");

        final Set<Path> reverse = IntStream.range(0, 10)
                .mapToObj(i -> new Path("/test/*")
                        .toPathWithContext(nodeId.toString())
                        .appendUUIDIfWildcard())
                .collect(Collectors.toSet());

        final var strategy = new ResourceEntry.OperationalStrategy() {

            @Override
            public Set<Path> doGetReversePathsImmutable(final ResourceEntry entry) {
                return reverse;
            }

            @Override
            public boolean doIsOriginalContent(ResourceEntry entry) {
                return false;
            }

            @Override
            public ResourceContents doUpdateResourceContents(ResourceEntry resourceEntry) {
                final var mapping = UnixFSResourceContentsMapping.fromResourceId(utils, resourceId);
                return new UnixFSTemporaryResourceContents(mapping);
            }

        };

        try (var entry = new UnixFSResourceEntryNew(utils, resourceId, strategy)) {

            try (var wbc = entry.updateResourceContents().write(journalEntry.getTransactionId())) {

                final var buffer = allocate(4096);
                while (buffer.hasRemaining()) buffer.put((byte)0xFF);
                buffer.flip();

                while (buffer.hasRemaining()) {
                    if (wbc.write(buffer) < 0) {
                        throw new IOException("Unexpected end of stream.");
                    }
                }

            }

            entry.flush(journalEntry);

        }

        final var resourceContentsMapping = fromResourceId(utils, resourceId);
        assertTrue(isRegularFile(resourceContentsMapping.getFilesystemPath(journalEntry.getTransactionId())));
        assertFalse(exists(resourceContentsMapping.getFilesystemPath()));

        utils.commit(resourceContentsMapping, journalEntry.getTransactionId());
        assertTrue(isRegularFile(resourceContentsMapping.getFilesystemPath()));
        assertFalse(exists(resourceContentsMapping.getFilesystemPath(journalEntry.getTransactionId())));

    }

    @Test(dependsOnMethods = {"testFlushContents", "testFlushReversePaths"})
    public void testRemove() throws IOException {

        final var journalEntry = mock(TransactionJournal.MutableEntry.class);
        when(journalEntry.getTransactionId()).thenReturn("test");

        final var strategy = new ResourceEntry.OperationalStrategy() {

            @Override
            public Set<Path> doGetReversePathsImmutable(final ResourceEntry entry) {
                return reverse;
            }

            @Override
            public Optional<ResourceId> doFindResourceId(ResourceEntry entry) {
                return Optional.empty();
            }

        };

        var contentsMapping = UnixFSResourceContentsMapping.fromResourceId(utils, resourceId);
        var reversePathMapping = UnixFSReversePathMapping.fromResourceId(utils, resourceId);

        try (var entry = new UnixFSResourceEntryExisting(utils, reversePathMapping, strategy)) {
            entry.flush(journalEntry);
        }

        assertTrue(utils.isTombstone(contentsMapping.getFilesystemPath(journalEntry.getTransactionId())));
        assertTrue(utils.isTombstone(reversePathMapping.getFilesystemPath(journalEntry.getTransactionId())));

        utils.commit(contentsMapping, journalEntry.getTransactionId());
        assertFalse(exists(contentsMapping.getFilesystemPath()));
        assertFalse(exists(contentsMapping.getFilesystemPath(journalEntry.getTransactionId())));

        utils.commit(reversePathMapping, journalEntry.getTransactionId());
        assertFalse(exists(reversePathMapping.getFilesystemPath()));
        assertFalse(exists(reversePathMapping.getFilesystemPath(journalEntry.getTransactionId())));

        for (var path : reverse) {

            final var pathMapping = UnixFSPathMapping.fromRTPath(utils, path);
            assertTrue(utils.isTombstone(pathMapping.getFilesystemPath(journalEntry.getTransactionId())));

            utils.commit(pathMapping, journalEntry.getTransactionId());
            assertFalse(exists(reversePathMapping.getFilesystemPath()));
            assertFalse(exists(reversePathMapping.getFilesystemPath(journalEntry.getTransactionId())));

        }



    }

}
