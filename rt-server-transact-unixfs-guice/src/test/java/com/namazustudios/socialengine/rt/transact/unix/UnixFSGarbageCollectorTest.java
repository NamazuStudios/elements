package com.namazustudios.socialengine.rt.transact.unix;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.ResourceLoader;
import com.namazustudios.socialengine.rt.ResourceService;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.transact.SimpleTransactionalResourceServicePersistenceModule;
import com.namazustudios.socialengine.rt.transact.TransactionalPersistenceContext;
import com.namazustudios.socialengine.rt.transact.TransactionalResourceService;
import com.namazustudios.socialengine.rt.transact.TransactionalResourceServiceModule;
import org.mockito.Mockito;
import org.testng.annotations.*;

import javax.inject.Inject;
import java.io.InputStream;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.namazustudios.socialengine.rt.id.NodeId.randomNodeId;
import static com.namazustudios.socialengine.rt.id.ResourceId.randomResourceIdForNode;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

@Guice(modules = UnixFSGarbageCollectorTest.Module.class)
public class UnixFSGarbageCollectorTest {

    @Inject
    private NodeId nodeId;

    @Inject
    private UnixFSUtils utils;

    @Inject
    private ResourceService resourceService;

    @Inject
    private UnixFSGarbageCollector garbageCollector;

    @Inject
    private TransactionalResourceService transactionalResourceService;

    @Inject
    private TransactionalPersistenceContext transactionalPersistenceContext;

    private final Queue<Throwable> garbageCollectionErrors = new ConcurrentLinkedQueue<>();

    @BeforeClass
    public void start() {
        transactionalPersistenceContext.start();
        transactionalResourceService.start();
        garbageCollector.setPaused(true);
        garbageCollector.setUncaughtExceptionHandler((thread, error) -> garbageCollectionErrors.add(error));
    }

    @AfterClass
    public void stop() {
        transactionalResourceService.stop();
        transactionalPersistenceContext.stop();
    }

    @DataProvider
    public Object[][] initialDataProvider() {

        final List<Object[]> testData = new ArrayList<>();

        for (int i = 0; i < 100; ++i) {
            final ResourceId resourceId = randomResourceIdForNode(nodeId);
            final Path path = new Path(nodeId.asString(), asList("test_a", randomUUID().toString()));
            testData.add(new Object[]{resourceId, path});
        }

        return testData.toArray(new Object[][]{});

    }

    @Test(dataProvider = "initialDataProvider")
    public void makeGarbage(final ResourceId resourceId, final Path aliasA) {

        final Resource resource = getMockResource(resourceId);
        resourceService.addAndAcquireResource(aliasA, resource);

        final Path aliasB = new Path(nodeId.asString(), asList("test_b", randomUUID().toString()));
        final Path aliasC = new Path(nodeId.asString(), asList("test_c", randomUUID().toString()));

        resourceService.link(resourceId, aliasB);
        resourceService.link(resourceId, aliasC);

        resourceService.unlinkPath(aliasA);
        resourceService.unlinkPath(aliasB);
        resourceService.unlinkPath(aliasC);

    }

    @Test(dependsOnMethods = "makeGarbage")
    public void forceCollectionCycle() {
        garbageCollector.forceSync();
        assertTrue(garbageCollectionErrors.isEmpty(), "Caught one or more garbage collection errors.");
    }

    public Resource getMockResource(final ResourceId resourceId) {
        final Resource resource = Mockito.mock(Resource.class);
        when(resource.getId()).thenReturn(resourceId);
        return resource;
    }

    public static class Module extends AbstractModule {

        @Override
        protected void configure() {

            final NodeId testNodeId = randomNodeId();

            bind(NodeId.class).toInstance(testNodeId);

            install(new TransactionalResourceServiceModule().exposeTransactionalResourceService());
            install(new SimpleTransactionalResourceServicePersistenceModule());

            install(new UnixFSTransactionalPersistenceContextModule()
                .exposeDetailsForTesting()
                .withTestingDefaults());

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
