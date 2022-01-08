package com.namazustudios.socialengine.rt.xodus;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.rt.PersistenceEnvironment;
import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.ResourceLoader;
import com.namazustudios.socialengine.rt.ResourceService;
import com.namazustudios.socialengine.rt.guice.AbstractResourceServiceLinkingUnitTest;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.transact.TransactionalResourceService;
import com.namazustudios.socialengine.rt.transact.TransactionalResourceServiceModule;
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

import static com.namazustudios.socialengine.rt.id.NodeId.randomNodeId;
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
