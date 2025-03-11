package dev.getelements.elements.rt.transact.unix;

import com.google.inject.AbstractModule;
import dev.getelements.elements.rt.PersistenceEnvironment;
import dev.getelements.elements.rt.Resource;
import dev.getelements.elements.rt.ResourceLoader;
import dev.getelements.elements.rt.ResourceService;
import dev.getelements.elements.rt.guice.AbstractResourceServiceReleasingUnitTest;
import dev.getelements.elements.sdk.cluster.id.NodeId;
import dev.getelements.elements.sdk.cluster.id.ResourceId;
import dev.getelements.elements.rt.transact.JournalTransactionalResourceServicePersistenceModule;
import dev.getelements.elements.rt.transact.TransactionalResourceService;
import dev.getelements.elements.rt.transact.TransactionalResourceServiceModule;
import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;

import jakarta.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import static dev.getelements.elements.sdk.cluster.id.NodeId.randomNodeId;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

@Guice(modules = UnixFSResourceServiceReleasingUnitTest.Module.class)
public class UnixFSResourceServiceReleasingUnitTest extends AbstractResourceServiceReleasingUnitTest {

    @jakarta.inject.Inject
    private PersistenceEnvironment persistenceEnvironment;

    @Inject
    private TransactionalResourceService transactionalResourceService;

    @Override
    public ResourceService getResourceService() {
        return transactionalResourceService;
    }

    @BeforeClass
    public void start() {
        persistenceEnvironment.start();
        transactionalResourceService.start();
    }

    @AfterClass
    public void stop() {
        transactionalResourceService.stop();
        persistenceEnvironment.stop();
    }

    @Override
    public Resource getMockResource(final ResourceId resourceId) {
        return doGetMockResource(resourceId);
    }

    public static class Module extends AbstractModule {

        @Override
        protected void configure() {

            final NodeId testNodeId = randomNodeId();

            bind(NodeId.class).toInstance(testNodeId);

            install(new TransactionalResourceServiceModule().exposeTransactionalResourceService());
            install(new JournalTransactionalResourceServicePersistenceModule());

            install(new UnixFSTransactionalPersistenceContextModule()
                    .exposeDetailsForTesting()
                    .withTestingDefaults());

            final var resourceLoader = mock(ResourceLoader.class);

            doAnswer(a -> {

                final ReadableByteChannel rbc = a.getArgument(0);
                final ByteBuffer byteBuffer = ByteBuffer.allocate(ResourceId.getSizeInBytes());
                while (byteBuffer.hasRemaining() && rbc.read(byteBuffer) >= 0);
                byteBuffer.rewind();

                final ResourceId resourceId = ResourceId.resourceIdFromByteBuffer(byteBuffer);
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
                final var wbc = (WritableByteChannel) a.getArgument(0);
                final var bytes = resourceId.asBytes();
                final var buffer = ByteBuffer.wrap(bytes);
                while (buffer.hasRemaining()) wbc.write(buffer);
                return null;
            }).when(resource).serialize(any(WritableByteChannel.class));
        } catch (IOException e) {
            // Should never happen in test code unless something is really wrong
            throw new UncheckedIOException(e);
        }

        return resource;

    }

}
