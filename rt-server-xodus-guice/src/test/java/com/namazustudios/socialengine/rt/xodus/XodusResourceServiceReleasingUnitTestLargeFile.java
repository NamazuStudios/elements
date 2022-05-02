package com.namazustudios.socialengine.rt.xodus;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.rt.PersistenceEnvironment;
import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.ResourceLoader;
import com.namazustudios.socialengine.rt.ResourceService;
import com.namazustudios.socialengine.rt.guice.AbstractResourceServiceReleasingUnitTest;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.transact.TransactionalResourceService;
import com.namazustudios.socialengine.rt.transact.TransactionalResourceServiceModule;
import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;

import javax.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.ThreadLocalRandom;
import java.util.zip.Adler32;

import static com.namazustudios.socialengine.rt.id.NodeId.randomNodeId;
import static com.namazustudios.socialengine.rt.id.ResourceId.resourceIdFromByteBuffer;
import static com.namazustudios.socialengine.rt.xodus.XodusTransactionalResourceServicePersistenceEnvironment.DEFAULT_RESOURCE_BLOCK_SIZE;
import static java.lang.Integer.min;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Guice(modules = XodusResourceServiceReleasingUnitTestLargeFile.Module.class)
public class XodusResourceServiceReleasingUnitTestLargeFile extends AbstractResourceServiceReleasingUnitTest {

    private static final int FILE_SIZE_MAX = 1024 * 1024;

    private static final int FILE_SIZE_MIN = ResourceId.getSizeInBytes() + Integer.BYTES;

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

                final var crc = new Adler32();
                final var rbc = (ReadableByteChannel) a.getArgument(0);
                final var buffer = ByteBuffer.allocate(FILE_SIZE_MAX);

                while (rbc.read(buffer) >= 0) {
                    assertTrue(buffer.hasRemaining(), "Data store supplied more than expected data.");
                }

                buffer.flip();

                final var originalCrcValue = buffer.getInt() & 0x00000000FFFFFFFFL;

                crc.update(buffer.rewind().putInt(0).rewind());

                assertEquals(originalCrcValue, crc.getValue(), "CRC Mismatch");
                buffer.position(Integer.BYTES);

                final var resourceId = resourceIdFromByteBuffer(buffer);
                return doGetMockResource(resourceId);

            }).when(resourceLoader).load(any(ReadableByteChannel.class), anyBoolean());

            bind(ResourceLoader.class).toInstance(resourceLoader);

        }

    }

    private static Resource doGetMockResource(final ResourceId resourceId) {

        final Resource resource = Mockito.mock(Resource.class);
        when(resource.getId()).thenReturn(resourceId);

        try {
            doAnswer(a -> {

                // This makes a randomly large file to actually simulate what the data may look like in production.
                // We fill the randomly sized file with a bunch of random junk. We CRC that random junk to ensure that
                // when read back out the contents are preserved without error.

                final var wbc = (WritableByteChannel) a.getArgument(0);

                final var crc = new Adler32();
                final var random = ThreadLocalRandom.current();

                final var block = ByteBuffer.allocate( (int) DEFAULT_RESOURCE_BLOCK_SIZE);
                final var buffer = ByteBuffer.allocate(random.nextInt(FILE_SIZE_MIN, FILE_SIZE_MAX));

                block.putInt(0);
                block.put(resourceId.asBytes());

                while (block.hasRemaining()) block.put((byte) random.nextInt());
                buffer.put(block.position(0).limit(min(block.remaining(), buffer.remaining())));

                for (var sequence = 1L; buffer.hasRemaining(); ++sequence) {
                    block.clear().putInt((int) sequence);
                    while (block.hasRemaining()) block.put((byte) random.nextInt());
                    buffer.put(block.position(0).limit(min(block.remaining(), buffer.remaining())));
                }

                crc.update(buffer.flip());

                final int crcValue = (int) crc.getValue();
                buffer.rewind().putInt(crcValue).rewind();

                while (buffer.hasRemaining()) wbc.write(buffer);
                assertEquals(buffer.limit(), buffer.capacity(), "Expected full buffer to be consumed.");

                crc.reset();
                buffer.rewind().putInt(0).rewind();
                crc.update(buffer.rewind());

                final int recalculatedCrcValue = (int) crc.getValue();
                assertEquals(recalculatedCrcValue, crcValue);

                return null;

            }).when(resource).serialize(any(WritableByteChannel.class));
        } catch (IOException e) {
            // Should never happen in test code unless something is really wrong
            throw new UncheckedIOException(e);
        }

        return resource;

    }

}
