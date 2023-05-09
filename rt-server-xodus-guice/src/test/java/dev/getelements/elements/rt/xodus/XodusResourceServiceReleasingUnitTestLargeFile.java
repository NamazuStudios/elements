package dev.getelements.elements.rt.xodus;

import com.google.inject.AbstractModule;
import dev.getelements.elements.rt.PersistenceEnvironment;
import dev.getelements.elements.rt.Resource;
import dev.getelements.elements.rt.ResourceLoader;
import dev.getelements.elements.rt.ResourceService;
import dev.getelements.elements.rt.guice.AbstractResourceServiceReleasingUnitTest;
import dev.getelements.elements.rt.id.NodeId;
import dev.getelements.elements.rt.id.ResourceId;
import dev.getelements.elements.rt.transact.TransactionalResourceService;
import dev.getelements.elements.rt.transact.TransactionalResourceServiceModule;
import dev.getelements.elements.rt.util.TemporaryFiles;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;

import javax.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

import static dev.getelements.elements.rt.id.NodeId.randomNodeId;
import static java.nio.channels.FileChannel.open;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

@Guice(modules = XodusResourceServiceReleasingUnitTestLargeFile.Module.class)
public class XodusResourceServiceReleasingUnitTestLargeFile extends AbstractResourceServiceReleasingUnitTest {

    private static final Logger logger = LoggerFactory.getLogger(XodusResourceServiceReleasingUnitTestLargeFile.class);

    private static final int FILE_SIZE_MAX = 1024 * 1024;

    private static final int FILE_SIZE_MIN = ResourceId.getSizeInBytes();

    private static final TemporaryFiles temporaryFiles = new TemporaryFiles(XodusResourceServiceReleasingUnitTestLargeFile.class);

    @Inject
    private PersistenceEnvironment persistence;

    @Inject
    private TransactionalResourceService transactionalResourceService;

    @Override
    public ResourceService getResourceService() {
        return transactionalResourceService;
    }

    @BeforeClass
    public void start() {
        persistence.start();
        transactionalResourceService.start();
    }

    @AfterClass
    public void stop() {
        transactionalResourceService.stop();
        persistence.stop();
    }

    @Override
    public Resource getMockResource(final ResourceId resourceId) {
        return doGetMockResource(resourceId);
    }

    public static class Module extends AbstractModule {

        @Override
        protected void configure() {

            final var testNodeId = randomNodeId();

            bind(NodeId.class).toInstance(testNodeId);

            install(new XodusEnvironmentModule()
                    .withTempSchedulerEnvironment()
                    .withTempResourceEnvironment()
            );

            install(new TransactionalResourceServiceModule().exposeTransactionalResourceService());
            install(new XodusTransactionalResourceServicePersistenceModule().withDefaultBlockSize());

            final var resourceLoader = mock(ResourceLoader.class);

            doAnswer(a -> {

                final var rbc = (ReadableByteChannel) a.getArgument(0);
                final var buffer = ByteBuffer.allocate(FILE_SIZE_MAX);

                while (rbc.read(buffer) >= 0) {
                    assertTrue(buffer.hasRemaining(), "Data store supplied more than expected data.");
                }

                buffer.flip();

                final var resourceId = ResourceId.resourceIdFromByteBuffer(buffer);
                return doGetMockResource(resourceId);

            }).when(resourceLoader).load(any(ReadableByteChannel.class), anyBoolean());

            bind(ResourceLoader.class).toInstance(resourceLoader);

        }

    }

    private static Resource doGetMockResource(final ResourceId resourceId) {

        final var resource = Mockito.mock(Resource.class);
        when(resource.getId()).thenReturn(resourceId);

        final var current = new AtomicReference<Thread>();

        try {
            doAnswer(a -> {

                final var thread = Thread.currentThread();
                assertTrue(current.compareAndSet(null, thread), "Concurrent access.");

                final var seed = ThreadLocalRandom.current().nextLong();
                final var temporaryPath = temporaryFiles.createTempFile();
                final var datastoreWritableByteChannel = (WritableByteChannel) a.getArgument(0);

                try (final var temporaryWritableByteChannel = open(temporaryPath, WRITE)) {

                    final var random = new Random();
                    random.setSeed(seed);
                    write(random, resourceId, temporaryWritableByteChannel);
                    random.setSeed(seed);
                    write(random, resourceId, datastoreWritableByteChannel);

                } finally {
                    assertTrue(current.compareAndSet(thread, null), "Concurrent access.");
                }

                return null;

            }).when(resource).serialize(any(WritableByteChannel.class));
        } catch (IOException e) {
            // Should never happen in test code unless something is really wrong
            throw new UncheckedIOException(e);
        }

        return resource;

    }

    private static void write(final Random random,
                              final ResourceId resourceId,
                              final WritableByteChannel wbc) throws Exception {

        // This makes a randomly large file to actually simulate what the data may look like in production.

        final var size = random.nextInt( (FILE_SIZE_MAX - FILE_SIZE_MIN) + 1) + FILE_SIZE_MIN;
        final var buffer = ByteBuffer.allocate(size);

        resourceId.toByteBuffer(buffer);

        while (buffer.hasRemaining()) {
            final var value = (byte) random.nextInt(Byte.MAX_VALUE);
            buffer.put(value);
        }

        buffer.flip();
        while (buffer.hasRemaining()) wbc.write(buffer);

    }

}
