package dev.getelements.elements.rt.xodus;

import com.google.inject.AbstractModule;
import dev.getelements.elements.rt.PersistenceEnvironment;
import dev.getelements.elements.rt.Resource;
import dev.getelements.elements.rt.ResourceLoader;
import dev.getelements.elements.rt.ResourceService;
import dev.getelements.elements.rt.guice.AbstractResourceServiceLinkingUnitTest;
import dev.getelements.elements.rt.id.NodeId;
import dev.getelements.elements.rt.id.ResourceId;
import dev.getelements.elements.rt.transact.TransactionalResourceService;
import dev.getelements.elements.rt.transact.TransactionalResourceServiceModule;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;

import javax.inject.Inject;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

import static dev.getelements.elements.rt.id.NodeId.randomNodeId;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Guice(modules = XodusResourceServiceLinkingUnitTest.Module.class)
public class XodusResourceServiceLinkingUnitTest extends AbstractResourceServiceLinkingUnitTest {

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

            final Answer<Resource> loadAnswer = a -> {

                final ReadableByteChannel rbc = a.getArgument(0);
                final ByteBuffer byteBuffer = ByteBuffer.allocate(ResourceId.getSizeInBytes());
                while (byteBuffer.hasRemaining() && rbc.read(byteBuffer) >= 0);
                byteBuffer.rewind();

                final ResourceId resourceId = ResourceId.resourceIdFromByteBuffer(byteBuffer);
                return doGetMockResource(resourceId);

            };

            doAnswer(loadAnswer).when(resourceLoader).load(any(ReadableByteChannel.class));
            doAnswer(loadAnswer).when(resourceLoader).load(any(ReadableByteChannel.class), anyBoolean());

            bind(ResourceLoader.class).toInstance(resourceLoader);

        }

        private static Resource doGetMockResource(final ResourceId resourceId) {

            final Resource resource = Mockito.mock(Resource.class);
            when(resource.getId()).thenReturn(resourceId);

            try {
                doAnswer(a -> {
                    final OutputStream os = a.getArgument(0);
                    final byte[] bytes = resourceId.asString().getBytes(UTF_8);
                    os.write(bytes);
                    return null;
                }).when(resource).serialize(any(OutputStream.class));
            } catch (IOException e) {
                // Should never happen in test code unless something is really wrong
                throw new UncheckedIOException(e);
            }

            return resource;

        }

    }

}
