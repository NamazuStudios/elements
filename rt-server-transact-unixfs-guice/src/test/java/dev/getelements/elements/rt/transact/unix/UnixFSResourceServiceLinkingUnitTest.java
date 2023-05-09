package dev.getelements.elements.rt.transact.unix;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import dev.getelements.elements.rt.PersistenceEnvironment;
import dev.getelements.elements.rt.Resource;
import dev.getelements.elements.rt.ResourceLoader;
import dev.getelements.elements.rt.ResourceService;
import dev.getelements.elements.rt.guice.AbstractResourceServiceLinkingUnitTest;
import dev.getelements.elements.rt.id.NodeId;
import dev.getelements.elements.rt.id.ResourceId;
import dev.getelements.elements.rt.transact.JournalTransactionalResourceServicePersistenceModule;
import dev.getelements.elements.rt.transact.TransactionalResourceService;
import dev.getelements.elements.rt.transact.TransactionalResourceServiceModule;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.rt.id.NodeId.randomNodeId;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UnixFSResourceServiceLinkingUnitTest extends AbstractResourceServiceLinkingUnitTest {

    private static final String GC_ENABLE = "dev.getelements.elements.rt.transact.unix.UnixFSResourceServiceAcquiringUnitTest.gc.enable";

    @Factory
    public static Object[] getTests() {
        return new Object[] {
                withGarbageCollectionEnabled(),
                withGarbageCollectionDisabled()
        };
    }

    private static UnixFSResourceServiceLinkingUnitTest withGarbageCollectionEnabled() {
        final Module module = new Module("gc-enabled", true);
        final Injector injector = Guice.createInjector(module);
        return injector.getInstance(UnixFSResourceServiceLinkingUnitTest.class);
    }

    private static UnixFSResourceServiceLinkingUnitTest withGarbageCollectionDisabled() {
        final Module module = new Module("gc-disabled", false);
        final Injector injector = Guice.createInjector(module);
        return injector.getInstance(UnixFSResourceServiceLinkingUnitTest.class);
    }

    @Inject
    @Named(GC_ENABLE)
    private boolean gcEnable;

    @Inject
    private PersistenceEnvironment persistenceEnvironment;

    @Inject
    private UnixFSGarbageCollector garbageCollector;

    @Inject
    private TransactionalResourceService transactionalResourceService;

    @Override
    public ResourceService getResourceService() {
        return transactionalResourceService;
    }

    @BeforeClass
    public void start() {
        persistenceEnvironment.start();
        garbageCollector.setPaused(!gcEnable);
        transactionalResourceService.start();
    }

    @AfterClass
    public void stop() {
        transactionalResourceService.stop();
        persistenceEnvironment.stop();
    }

    public static class Module extends AbstractModule {

        private final String name;

        private final boolean gcEnable;

        public Module(final String name, final boolean gcEnable) {
            this.name = name;
            this.gcEnable = gcEnable;
        }

        @Override
        protected void configure() {

            final NodeId testNodeId = randomNodeId();

            bind(NodeId.class).toInstance(testNodeId);
            bind(boolean.class).annotatedWith(named(GC_ENABLE)).toInstance(gcEnable);

            install(new TransactionalResourceServiceModule().exposeTransactionalResourceService());
            install(new JournalTransactionalResourceServicePersistenceModule());

            install(new UnixFSTransactionalPersistenceContextModule()
                    .exposeDetailsForTesting()
                    .withTestingDefaults(name));

            final ResourceLoader resourceLoader = mock(ResourceLoader.class);

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
