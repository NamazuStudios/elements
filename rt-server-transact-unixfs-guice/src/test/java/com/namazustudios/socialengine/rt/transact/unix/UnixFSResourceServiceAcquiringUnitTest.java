package com.namazustudios.socialengine.rt.transact.unix;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.namazustudios.socialengine.rt.PersistenceEnvironment;
import com.namazustudios.socialengine.rt.ResourceLoader;
import com.namazustudios.socialengine.rt.ResourceService;
import com.namazustudios.socialengine.rt.guice.AbstractResourceServiceAcquiringUnitTest;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.transact.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.InputStream;
import java.nio.channels.ReadableByteChannel;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.id.NodeId.randomNodeId;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.fail;

public class UnixFSResourceServiceAcquiringUnitTest extends AbstractResourceServiceAcquiringUnitTest {

    private static final String GC_ENABLE = "com.namazustudios.socialengine.rt.transact.unix.UnixFSResourceServiceAcquiringUnitTest.gc.enable";

    @Factory
    public static Object[] getTests() {
        return new Object[] {
            withGarbageCollectionEnabled(),
            withGarbageCollectionDisabled()
        };
    }

    private static Object withGarbageCollectionEnabled() {
        final Module module = new Module("gc-enabled", true);
        final Injector injector = Guice.createInjector(module);
        return injector.getInstance(UnixFSResourceServiceAcquiringUnitTest.class);
    }

    private static Object withGarbageCollectionDisabled() {
        final Module module = new Module("gc-disabled", false);
        final Injector injector = Guice.createInjector(module);
        return injector.getInstance(UnixFSResourceServiceAcquiringUnitTest.class);
    }

    @Inject
    @Named(GC_ENABLE)
    private boolean gcEnable;

    @Inject
    private PersistenceEnvironment persistence;

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
        persistence.start();
        garbageCollector.setPaused(!gcEnable);
        transactionalResourceService.start();
    }

    @AfterClass
    public void stop() {
        transactionalResourceService.stop();
        persistence.stop();
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

            doAnswer(a -> {
                fail("No attempt to load resource should be made for this test.");
                return null;
            }).when(resourceLoader).load(any(InputStream.class));

            doAnswer(a -> {
                fail("No attempt to load resource should be made for this test.");
                return null;
            }).when(resourceLoader).load(any(ReadableByteChannel.class));

            bind(ResourceLoader.class).toInstance(resourceLoader);

        }

    }

}
