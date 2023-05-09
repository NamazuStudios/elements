package dev.getelements.elements.rt.guice;

import dev.getelements.elements.rt.Path;
import dev.getelements.elements.rt.Resource;
import dev.getelements.elements.rt.ResourceService;
import dev.getelements.elements.rt.id.NodeId;
import dev.getelements.elements.rt.id.ResourceId;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static dev.getelements.elements.rt.id.ResourceId.randomResourceIdForNode;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.FileAssert.fail;

public abstract class AbstractResourceServiceLinkingUnitTest {

    private static final Logger logger = LoggerFactory.getLogger(AbstractResourceServiceLinkingUnitTest.class);

    private final List<Object[]> intermediates = new CopyOnWriteArrayList<>();

    @Inject
    private NodeId nodeId;

    @DataProvider
    public Object[][] initialDataProvider() {

        final List<Object[]> testData = new ArrayList<>();

        for (int i = 0; i < 100; ++i) {
            final ResourceId resourceId = randomResourceIdForNode(nodeId);
            final Path path = new Path(asList("test", randomUUID().toString()));
            final Path alias = new Path(asList("test", randomUUID().toString()));
            testData.add(new Object[]{resourceId, path, alias});
        }

        return testData.toArray(new Object[][]{});

    }

    @Test(dataProvider = "initialDataProvider")
    public void testAdd(final ResourceId resourceId, final Path path, final Path alias) throws IOException {

        final Resource resource = Mockito.mock(Resource.class);
        Mockito.when(resource.getId()).thenReturn(resourceId);

        doAnswer(a -> {
            final WritableByteChannel wbc = a.getArgument(0);
            final byte[] resourceIdBytes = resourceId.asBytes();
            wbc.write(ByteBuffer.wrap(resourceIdBytes));
            return null;
        }).when(resource).serialize(any(WritableByteChannel.class));

        getResourceService().addAndReleaseResource(path, resource);
        getResourceService().link(resourceId, alias);

        intermediates.add(new Object[]{resourceId, path, alias, resource});

    }

    @DataProvider(parallel = true)
    public Object[][] intermediateDataProvider() {
        return intermediates.toArray(new Object[][]{});
    }

    @Test(dependsOnMethods = "testAdd", dataProvider = "intermediateDataProvider")
    public void testRemoveAllAliases(final ResourceId resourceId, final Path path, final Path alias, final Resource resource) {

        final ResourceService.Unlink first = getResourceService().unlinkPath(path, r -> fail("Unexpected removal."));
        final ResourceService.Unlink second = getResourceService().unlinkPath(alias, r -> logger.info("Removed resource{} ", r.getId()));

        assertFalse(first.isRemoved(), "Resource should not have been removed on first call.");
        assertTrue(second.isRemoved(), "Resource shoudl have been removed on second call.");
        assertEquals(first.getResourceId(), resourceId);
        assertEquals(second.getResourceId(), resourceId);

    }

    public abstract ResourceService getResourceService();

}
