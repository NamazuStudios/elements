package com.namazustudios.socialengine.rt.guice;

import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.ResourceId;
import com.namazustudios.socialengine.rt.ResourceService;
import org.mockito.Mockito;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.FileAssert.fail;

@Guice(modules = SimpleResourceServiceUnitTest.Module.class)
public class SimpleResourceServiceLinkingUnitTest {

    private ResourceService resourceService;

    private final List<Object[]> intermediates = new ArrayList<>();

    @DataProvider
    public Object[][] initialDataProvider() {

        final List<Object[]> testData = new ArrayList<>();

        for (int i = 0; i < 100; ++i) {
            final ResourceId resourceId = new ResourceId();
            final Path path = new Path(asList("test", randomUUID().toString()));
            final Path alias = new Path(asList("test", randomUUID().toString()));
            testData.add(new Object[]{resourceId, path, alias});
        }

        return testData.toArray(new Object[][]{});

    }

    @Test(dataProvider = "initialDataProvider")
    public void testAdd(final ResourceId resourceId, final Path path, final Path alias) {

        final Resource resource = Mockito.mock(Resource.class);
        Mockito.when(resource.getId()).thenReturn(resourceId);

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
        final AtomicBoolean removed = new AtomicBoolean();

        final ResourceService.Unlink first = getResourceService().unlinkPath(path, r -> fail("Unexpected removal."));
        final ResourceService.Unlink second = getResourceService().unlinkPath(alias, r -> {
            assertEquals(r, resource);
            assertEquals(r.getId(), resourceId);
            removed.set(true);
        });

        assertTrue(removed.get(), "Resource was not removed.");

        assertFalse(first.isRemoved(), "Resource should not have been removed on first call.");
        assertTrue(second.isRemoved(), "Resource shoudl have been removed on second call.");
        assertEquals(first.getResourceId(), resourceId);
        assertEquals(second.getResourceId(), resourceId);

    }

    public ResourceService getResourceService() {
        return resourceService;
    }

    @Inject
    public void setResourceService(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

}
