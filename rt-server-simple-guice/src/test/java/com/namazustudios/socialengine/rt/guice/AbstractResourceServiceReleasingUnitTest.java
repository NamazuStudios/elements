package com.namazustudios.socialengine.rt.guice;

import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.ResourceService;
import com.namazustudios.socialengine.rt.exception.ResourceNotFoundException;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.id.ResourceId;
import org.mockito.Mockito;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.namazustudios.socialengine.rt.id.ResourceId.randomResourceIdForNode;
import static java.util.Arrays.asList;
import static java.util.Comparator.comparing;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.AssertJUnit.fail;

public abstract class AbstractResourceServiceReleasingUnitTest {

    private final List<Object[]> intermediates = new ArrayList<>();

    private final List<Object[]> linkedIntermediates = new ArrayList<>();

    @Inject
    private NodeId nodeId;

    @DataProvider
    public Object[][] initialDataProvider() {

        final List<Object[]> testData = new ArrayList<>();

        for (int i = 0; i < 100; ++i) {
            final ResourceId resourceId = randomResourceIdForNode(nodeId);
            final Path path = new Path(asList("test", randomUUID().toString()));
            testData.add(new Object[]{resourceId, path});
        }

        return testData.toArray(new Object[][]{});

    }

    @Test(dataProvider = "initialDataProvider")
    public void testAdd(final ResourceId resourceId, final Path path) {

        final Resource resource = getMockResource(resourceId);

        getResourceService().addAndReleaseResource(path, resource);
        intermediates.add(new Object[]{resourceId, path, resource});

    }

    @DataProvider(parallel = true)
    public Object[][] intermediateDataProvider() {
        return intermediates.toArray(new Object[][]{});
    }

    @Test(dependsOnMethods = "testAdd")
    public void testList() {

        final Path path = new Path(asList("test", "*"));

        final var expectedPathList = intermediates.stream()
            .map(a -> ((Path) a[1]).toPathWithContext(nodeId.toString()))
            .sorted(Path::compareTo)
            .collect(toList());

        final var expectedResourceIdList = intermediates.stream()
            .map(a -> (ResourceId) a[0])
            .sorted(comparing(ResourceId::asString))
            .collect(toList());

        final var pathList = getResourceService().listStream(path)
            .map(ResourceService.Listing::getPath)
            .sorted(Path::compareTo)
            .collect(toList());

        final var resourceIdList = getResourceService().listStream(path)
            .map(ResourceService.Listing::getResourceId)
            .sorted(comparing(ResourceId::asString))
            .collect(toList());

        // Joining to strings just to make it easier to read diffs in IntelliJ

        assertEquals(
                resourceIdList
                        .stream()
                        .map(ResourceId::asString)
                        .collect(joining("\n")),
                expectedResourceIdList
                        .stream()
                        .map(ResourceId::asString)
                        .collect(joining("\n"))
        );

        assertEquals(
                pathList
                        .stream()
                        .map(Path::toString)
                        .collect(joining("\n")),
                expectedPathList
                        .stream()
                        .map(Path::toString)
                        .collect(joining("\n"))
        );

    }

    @Test(dependsOnMethods = "testAdd")
    public void testListParallel() {

        final Path path = new Path(asList("test", "*"));

        final var expectedPathList = intermediates.stream()
                .map(a -> ((Path) a[1]).toPathWithContext(nodeId.toString()))
                .sorted(Path::compareTo)
                .collect(toList());

        final var expectedResourceIdList = intermediates.stream()
                .map(a -> (ResourceId) a[0])
                .sorted(comparing(ResourceId::asString))
                .collect(toList());

        final var pathList = getResourceService().listParallelStream(path)
                .map(ResourceService.Listing::getPath)
                .sorted(Path::compareTo)
                .collect(toList());

        final var resourceIdList = getResourceService().listParallelStream(path)
                .map(ResourceService.Listing::getResourceId)
                .sorted(comparing(ResourceId::asString))
                .collect(toList());

        // Joining to strings just to make it easier to read diffs in IntelliJ

        assertEquals(
                resourceIdList
                        .stream()
                        .map(ResourceId::asString)
                        .collect(joining("\n")),
                expectedResourceIdList
                        .stream()
                        .map(ResourceId::asString)
                        .collect(joining("\n"))
        );

        assertEquals(
                pathList
                        .stream()
                        .map(Path::toString)
                        .collect(joining("\n")),
                expectedPathList
                        .stream()
                        .map(Path::toString)
                        .collect(joining("\n"))
        );

    }

    @Test(dependsOnMethods = "testAdd", dataProvider = "intermediateDataProvider")
    public void testGetResource(final ResourceId resourceId, final Path path, final Resource original) {

        final Resource acquired = getResourceService().getAndAcquireResourceAtPath(path);

        try {
            assertEquals(acquired.getId(), original.getId());
        } finally {
            getResourceService().release(acquired);
        }

    }

    @Test(dependsOnMethods = "testAdd", dataProvider = "intermediateDataProvider")
    public void testGetResourceAtPath(final ResourceId resourceId, final Path path, final Resource original) {

        final Resource acquired = getResourceService().getAndAcquireResourceAtPath(path);

        try {
            assertEquals(acquired.getId(), original.getId());
        } finally {
            getResourceService().release(acquired);
        }

    }

    @Test(dataProvider = "initialDataProvider", expectedExceptions = ResourceNotFoundException.class)
    public void testGetResourceFail(final ResourceId resourceId, final Path path) {

        final Resource acquired = getResourceService().getAndAcquireResourceWithId(resourceId);

        try {
            fail("Expected exception.");
        } finally {
            getResourceService().release(acquired);
        }

    }

    @Test(dataProvider = "initialDataProvider", expectedExceptions = ResourceNotFoundException.class)
    public void testGetResourceAtPathFail(final ResourceId resourceId, final Path path) {

        final Resource acquired = getResourceService().getAndAcquireResourceAtPath(path);

        try {
            fail("Expected exception.");
        } finally {
            getResourceService().release(acquired);
        }

    }

    @Test(dependsOnMethods = {"testAdd", "testGetResource", "testGetResourceAtPath"}, dataProvider = "intermediateDataProvider")
    public void testLink(final ResourceId resourceId, final Path path, final Resource original) {
        final Path alias = new Path(asList("test_alias", randomUUID().toString()));
        getResourceService().link(resourceId, alias);
        linkedIntermediates.add(new Object[]{resourceId, alias, original});
    }

    @Test(dependsOnMethods = {"testAdd", "testGetResource", "testGetResourceAtPath"}, dataProvider = "intermediateDataProvider")
    public void testLinkPath(final ResourceId resourceId, final Path path, final Resource original) {
        final Path alias = new Path(asList("test_alias", randomUUID().toString()));
        getResourceService().linkPath(path, alias);
        linkedIntermediates.add(new Object[]{resourceId, alias, original});
    }

    @DataProvider(parallel = true)
    public Object[][] linkedIntermediateProvider() {
        return linkedIntermediates.toArray(new Object[][]{});
    }

    @Test(dependsOnMethods = {"testLink", "testLinkPath"}, dataProvider = "linkedIntermediateProvider")
    public void testGetByAlias(final ResourceId resourceId, final Path path, final Resource original) {

        final Resource resource = getResourceService().getAndAcquireResourceAtPath(path);

        try {
            assertEquals(resource.getId(), original.getId());
        } finally {
            getResourceService().release(resource);
        }

    }

    @Test(dependsOnMethods = {"testGetByAlias"}, dataProvider = "linkedIntermediateProvider", expectedExceptions = ResourceNotFoundException.class)
    public void testUnlink(final ResourceId resourceId, final Path path, final Resource original) {

        final ResourceService.Unlink unlink;
        unlink = getResourceService().unlinkPath(path, removed -> fail("Did not expect resource removal."));

        assertEquals(resourceId, unlink.getResourceId(), "Unlink mismatch");
        assertFalse(unlink.isRemoved(), "Resource should not have been removed.");

        final Resource first = getResourceService().getAndAcquireResourceWithId(resourceId);

        try {
            assertEquals(first.getId(), original.getId());
        } finally {
            getResourceService().release(first);
        }

        final Resource second = getResourceService().getAndAcquireResourceAtPath(path);
        getResourceService().release(second);

    }

    @Test(dependsOnMethods = {"testUnlink"}, dataProvider = "intermediateDataProvider")
    public void testRemove(final ResourceId resourceId, final Path path, final Resource original) {

        getResourceService().removeResource(resourceId);

        try {
            final Resource resource = getResourceService().getAndAcquireResourceWithId(resourceId);
            getResourceService().release(resource);
            fail("Resource still exists");
        } catch (ResourceNotFoundException ex) {
            // Pass Test
        }

        try {
            final Resource resource = getResourceService().getAndAcquireResourceAtPath(path);
            getResourceService().release(resource);
            fail("Resource still exists");
        } catch (ResourceNotFoundException ex) {
            // Pass Test
        }

    }

    @Test(dependsOnMethods = {"testRemove"}, dataProvider = "intermediateDataProvider", expectedExceptions = ResourceNotFoundException.class)
    public void testDoubleRemove(final ResourceId resourceId, final Path path, final Resource original) {
        getResourceService().removeResource(resourceId);
    }

    @Test(dependsOnMethods = "testDoubleRemove")
    public void testAllPathsUnlinked() {
        final Stream<ResourceService.Listing> listingStream = getResourceService().listStream(new Path("*"));
        final List<ResourceService.Listing> listingList = listingStream.collect(toList());
        assertEquals(listingList.size(), 0);
    }

    @Test(dependsOnMethods = "testAllPathsUnlinked")
    public void testDeleteWithPaths() {

        final ResourceId resourceId = randomResourceIdForNode(nodeId);
        final Resource resource = getMockResource(resourceId);

        when(resource.getId()).thenReturn(resourceId);

        final Path path = new Path(randomUUID().toString());
        getResourceService().addAndReleaseResource(path, resource);

        final Path a = new Path(path, Path.fromComponents("a"));
        final Path b = new Path(path, Path.fromComponents("b"));
        getResourceService().link(resourceId, a);
        getResourceService().link(resourceId, b);

        getResourceService().destroy(resourceId);

        final Stream<ResourceService.Listing> listingStream = getResourceService().listStream(new Path("*"));
        final List<ResourceService.Listing> listingList = listingStream.collect(toList());
        assertEquals(listingList.size(), 0);

    }

    public abstract ResourceService getResourceService();

    public Resource getMockResource(final ResourceId resourceId) {
        final Resource resource = Mockito.mock(Resource.class);
        when(resource.getId()).thenReturn(resourceId);
        return resource;
    }

}
