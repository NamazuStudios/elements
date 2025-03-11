package dev.getelements.elements.rt.transact.unix;

import com.google.inject.AbstractModule;
import dev.getelements.elements.rt.PersistenceEnvironment;
import dev.getelements.elements.rt.ResourceLoader;
import dev.getelements.elements.rt.ResourceService;
import dev.getelements.elements.rt.guice.AbstractResourceServiceAcquiringUnitTest;
import dev.getelements.elements.sdk.cluster.id.NodeId;
import dev.getelements.elements.rt.transact.JournalTransactionalResourceServicePersistenceModule;
import dev.getelements.elements.rt.transact.TransactionalResourceService;
import dev.getelements.elements.rt.transact.TransactionalResourceServiceModule;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;

import jakarta.inject.Inject;
import java.io.InputStream;
import java.nio.channels.ReadableByteChannel;

import static dev.getelements.elements.sdk.cluster.id.NodeId.randomNodeId;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.fail;

@Guice(modules = UnixFSResourceServiceLinkingUnitTest.Module.class)
public class UnixFSResourceServiceAcquiringUnitTest extends AbstractResourceServiceAcquiringUnitTest {

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

            final NodeId testNodeId = randomNodeId();

            bind(NodeId.class).toInstance(testNodeId);

            install(new TransactionalResourceServiceModule()
                    .exposeTransactionalResourceService());

            install(new JournalTransactionalResourceServicePersistenceModule());

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