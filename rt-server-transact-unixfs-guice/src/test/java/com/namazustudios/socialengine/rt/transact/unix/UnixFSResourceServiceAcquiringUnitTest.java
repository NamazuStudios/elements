package com.namazustudios.socialengine.rt.transact.unix;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.rt.ResourceLoader;
import com.namazustudios.socialengine.rt.ResourceService;
import com.namazustudios.socialengine.rt.guice.AbstractResourceServiceAcquiringUnitTest;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.transact.SimpleTransactionalResourceServicePersistenceModule;
import com.namazustudios.socialengine.rt.transact.TransactionalResourceService;
import com.namazustudios.socialengine.rt.transact.TransactionalResourceServiceModule;
import org.testng.annotations.Guice;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;

import static com.namazustudios.socialengine.rt.id.NodeId.randomNodeId;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.fail;

@Guice(modules = {UnixFSResourceServiceAcquiringUnitTest.Module.class})
public class UnixFSResourceServiceAcquiringUnitTest extends AbstractResourceServiceAcquiringUnitTest {

    @Inject
    private TransactionalResourceService transactionalResourceService;

    @Override
    public ResourceService getResourceService() {
        return transactionalResourceService;
    }

    public static class Module extends AbstractModule {

        @Override
        protected void configure() {

            final NodeId testNodeId = randomNodeId();
            bind(NodeId.class).toInstance(testNodeId);

            install(new TransactionalResourceServiceModule());
            install(new SimpleTransactionalResourceServicePersistenceModule());

            try {
                install(new UnixFSTransactionalPersistenceContextModule().withTestingDefaults());
            } catch (IOException e) {
                addError(e);
            }

            final ResourceLoader resourceLoader = mock(ResourceLoader.class);

            doAnswer(a -> {
                fail("No attempt to load resource should be made for this test.");
                return null;
            }).when(resourceLoader).load(any(InputStream.class));

            bind(ResourceLoader.class).toInstance(resourceLoader);

        }

    }

}
