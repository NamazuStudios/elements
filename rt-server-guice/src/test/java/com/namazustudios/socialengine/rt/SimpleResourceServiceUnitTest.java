package com.namazustudios.socialengine.rt;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.namazustudios.socialengine.rt.exception.ResourceNotFoundException;
import com.namazustudios.socialengine.rt.guice.SimpleServicesModule;
import org.testng.annotations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toSet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.fail;

@Guice(modules = SimpleResourceServiceUnitTest.Module.class)
public class SimpleResourceServiceUnitTest {

    private ResourceService resourceService;

    private final List<Object[]> intermediates = new ArrayList<>();

    private final List<Object[]> linkedIntermediates = new ArrayList<>();

    @DataProvider
    public static Object[][] initialDataProvider() {

        final List<Object[]> testData = new ArrayList<>();

        for (int i = 0; i < 100; ++i) {
            final ResourceId resourceId = new ResourceId();
            final Path path = new Path(asList("test", randomUUID().toString()));
            testData.add(new Object[]{resourceId, path});
        }

        return testData.toArray(new Object[][]{});

    }

    @Test(dataProvider = "initialDataProvider")
    public void testAdd(final ResourceId resourceId, final Path path) {

        final Resource resource = mock(Resource.class);
        when(resource.getId()).thenReturn(resourceId);

        getResourceService().addResource(path, resource);
        intermediates.add(new Object[]{resourceId, path, resource});

    }

    @DataProvider(parallel = true)
    public Object[][] intermediateDataProvider() {
        return intermediates.toArray(new Object[][]{});
    }

    @Test(dependsOnMethods = "testAdd")
    public void testList() {

        final Path path = new Path(asList("test", "*"));

        final Set<ResourceId> expectedResourceIdList = intermediates.stream()
            .map(a -> (ResourceId) a[0])
            .collect(toSet());

        final Set<Path> expectedPathList = intermediates.stream()
                .map(a -> (Path) a[1])
                .collect(toSet());

        final Set<ResourceId> resourceIdList = getResourceService().listStream(path)
            .map(l -> l.getResourceId())
            .collect(toSet());

        final Set<Path> pathList = getResourceService().listStream(path)
                .map(l -> l.getPath())
                .collect(toSet());

        assertEquals(resourceIdList, expectedResourceIdList);
        assertEquals(pathList, expectedPathList);

    }

    @Test(dependsOnMethods = "testAdd")
    public void testListParallel() {

        final Path path = new Path(asList("test", "*"));

        final Set<ResourceId> expectedResourceIdList = intermediates.stream()
                .map(a -> (ResourceId) a[0])
                .collect(toSet());

        final Set<Path> expectedPathList = intermediates.stream()
                .map(a -> (Path) a[1])
                .collect(toSet());

        final Set<ResourceId> resourceIdList = getResourceService().listParallelStream(path)
                .map(l -> l.getResourceId())
                .collect(toSet());

        final Set<Path> pathList = getResourceService().listParallelStream(path)
                .map(l -> l.getPath())
                .collect(toSet());

        assertEquals(resourceIdList, expectedResourceIdList);
        assertEquals(pathList, expectedPathList);

    }


    @Test(dependsOnMethods = "testAdd", dataProvider = "intermediateDataProvider")
    public void testGetResource(final ResourceId resourceId, final Path path, final Resource resource) {
        assertEquals(getResourceService().getResourceWithId(resourceId), resource);
    }

    @Test(dependsOnMethods = "testAdd", dataProvider = "intermediateDataProvider")
    public void testGetResourceAtPath(final ResourceId resourceId, final Path path, final Resource resource) {
        assertEquals(getResourceService().getResourceAtPath(path), resource);
    }

    @Test(dataProvider = "initialDataProvider", expectedExceptions = ResourceNotFoundException.class)
    public void testGetResourceFail(final ResourceId resourceId, final Path path) {
        getResourceService().getResourceWithId(resourceId);
    }

    @Test(dataProvider = "initialDataProvider", expectedExceptions = ResourceNotFoundException.class)
    public void testGetResourceAtPathFail(final ResourceId resourceId, final Path path) {
        getResourceService().getResourceAtPath(path);
    }

    @Test(dependsOnMethods = {"testAdd", "testGetResource", "testGetResourceAtPath"}, dataProvider = "intermediateDataProvider")
    public void testLink(final ResourceId resourceId, final Path path, final Resource resource) {
        final Path alias = new Path(asList("test_alias", randomUUID().toString()));
        getResourceService().link(resourceId, alias);
        linkedIntermediates.add(new Object[]{resourceId, alias, resource});
    }

    @Test(dependsOnMethods = {"testAdd", "testGetResource", "testGetResourceAtPath"}, dataProvider = "intermediateDataProvider")
    public void testLinkPath(final ResourceId resourceId, final Path path, final Resource resource) {
        final Path alias = new Path(asList("test_alias", randomUUID().toString()));
        getResourceService().linkPath(path, alias);
        linkedIntermediates.add(new Object[]{resourceId, alias, resource});
    }

    @DataProvider(parallel = true)
    public Object[][] linkedIntermediateProvider() {
        return linkedIntermediates.toArray(new Object[][]{});
    }

    @Test(dependsOnMethods = {"testLink", "testLinkPath"}, dataProvider = "linkedIntermediateProvider")
    public void testGetByAlias(final ResourceId resourceId, final Path path, final Resource resource) {
        assertEquals(getResourceService().getResourceAtPath(path), resource);
    }

    @Test(dependsOnMethods = {"testGetByAlias"}, dataProvider = "linkedIntermediateProvider", expectedExceptions = ResourceNotFoundException.class)
    public void testUnlink(final ResourceId resourceId, final Path path, final Resource resource) {
        getResourceService().unlinkPath(path, removed -> fail("Did not expect resource removal."));
        assertEquals(getResourceService().getResourceWithId(resourceId), resource);
        getResourceService().getResourceAtPath(path);
    }

    @Test(dependsOnMethods = {"testUnlink"}, dataProvider = "intermediateDataProvider")
    public void testRemove(final ResourceId resourceId, final Path path, final Resource resource) {

        getResourceService().removeResource(resourceId);

        try {
            getResourceService().getResourceWithId(resourceId);
            fail("Resource still exists");
        } catch (ResourceNotFoundException ex) {
            // Pass Test
        }

        try {
            getResourceService().getResourceAtPath(path);
            fail("Resource still exists");
        } catch (ResourceNotFoundException ex) {
            // Pass Test
        }

    }

    @Test(dependsOnMethods = {"testRemove"}, dataProvider = "intermediateDataProvider", expectedExceptions = ResourceNotFoundException.class)
    public void testDoubleRemove(final ResourceId resourceId, final Path path, final Resource resource) {
        getResourceService().removeResource(resourceId);
    }

    public ResourceService getResourceService() {
        return resourceService;
    }

    @Inject
    public void setResourceService(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    public static class Module extends AbstractModule {

        @Override
        protected void configure() {

            install(new SimpleServicesModule());

            final AssetLoader mockAssetLoader = mock(AssetLoader.class);
            bind(AssetLoader.class).toInstance(mockAssetLoader);

            final ResourceLoader resourceLoader = mock(ResourceLoader.class);
            bind(ResourceLoader.class).toInstance(resourceLoader);

        }

    }
}
