package dev.getelements.elements.rt.guice;

import dev.getelements.elements.sdk.cluster.path.Path;
import dev.getelements.elements.rt.Resource;
import dev.getelements.elements.rt.ResourceService;
import dev.getelements.elements.rt.exception.ResourceNotFoundException;
import dev.getelements.elements.sdk.cluster.id.NodeId;
import dev.getelements.elements.sdk.cluster.id.ResourceId;
import org.mockito.Mockito;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

import static dev.getelements.elements.sdk.cluster.path.Paths.WILDCARD_FIRST;
import static dev.getelements.elements.sdk.cluster.id.ResourceId.randomResourceIdForNode;
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

    private final List<Object[]> intermediates = new CopyOnWriteArrayList<>();

    private final List<Object[]> linkedIntermediates = new CopyOnWriteArrayList<>();

    @Inject
    private NodeId nodeId;

    @DataProvider
    public Object[][] initialDataProvider() {

        final List<Object[]> testData = new ArrayList<>();

        for (int i = 0; i < 30; ++i) {
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
            .sorted(WILDCARD_FIRST)
            .collect(toList());

        final var expectedResourceIdList = intermediates.stream()
            .map(a -> (ResourceId) a[0])
            .sorted(comparing(ResourceId::asString))
            .collect(toList());

        final var pathList = getResourceService().listStream(path)
            .map(ResourceService.Listing::getPath)
            .sorted(WILDCARD_FIRST)
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
                .sorted(WILDCARD_FIRST)
                .collect(toList());

        final var expectedResourceIdList = intermediates.stream()
                .map(a -> (ResourceId) a[0])
                .sorted(comparing(ResourceId::asString))
                .collect(toList());

        final var pathList = getResourceService().listStream(path)
                .map(ResourceService.Listing::getPath)
                .sorted(WILDCARD_FIRST)
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

    @Test(dependsOnMethods = "testAdd", dataProvider = "intermediateDataProvider")
    public void testGetResource(final ResourceId resourceId, final Path path, final Resource original) {
        try (var acquired = getResourceService().acquire(resourceId)) {
            assertEquals(acquired.getResourceId(), original.getId());
        }
    }

    @Test(dependsOnMethods = "testAdd", dataProvider = "intermediateDataProvider")
    public void testGetResourceAtPath(final ResourceId resourceId, final Path path, final Resource original) {
        try (var acquired = getResourceService().acquire(path)) {
            assertEquals(acquired.getResourceId(), original.getId());
        }
    }

    @Test(dataProvider = "initialDataProvider", expectedExceptions = ResourceNotFoundException.class)
    public void testGetResourceFail(final ResourceId resourceId, final Path path) {
        try (var acquired = getResourceService().acquire(resourceId)) {
            fail("Expected exception.");
        }
    }

    @Test(dataProvider = "initialDataProvider", expectedExceptions = ResourceNotFoundException.class)
    public void testGetResourceAtPathFail(final ResourceId resourceId, final Path path) {
        try (var acquired = getResourceService().acquire(path)) {
            fail("Expected exception.");
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
        try (var acquisition = getResourceService().acquire(path) ){
            assertEquals(acquisition.getResourceId(), original.getId());
        }
    }

    @Test(dependsOnMethods = {"testGetByAlias"}, dataProvider = "linkedIntermediateProvider", expectedExceptions = ResourceNotFoundException.class)
    public void testUnlink(final ResourceId resourceId, final Path path, final Resource original) {

        final ResourceService.Unlink unlink;
        unlink = getResourceService().unlinkPath(path, removed -> fail("Did not expect resource removal."));

        assertEquals(resourceId, unlink.getResourceId(), "Unlink mismatch");
        assertFalse(unlink.isRemoved(), "Resource should not have been removed.");

        try (var first = getResourceService().acquire(resourceId)){
            assertEquals(first.getResourceId(), original.getId());
        }

        try (var second = getResourceService().acquire(path)) {
            fail("Expected exception.");
        }

    }

    @Test(dependsOnMethods = {"testUnlink"}, dataProvider = "intermediateDataProvider")
    public void testRemove(final ResourceId resourceId, final Path path, final Resource original) {

        getResourceService().removeResource(resourceId);

        try (var acquisition = getResourceService().acquire(resourceId)) {
            fail("Resource still exists");
        } catch (ResourceNotFoundException ex) {
            // Pass Test
        }

        try (var acquisition = getResourceService().acquire(path)) {
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
