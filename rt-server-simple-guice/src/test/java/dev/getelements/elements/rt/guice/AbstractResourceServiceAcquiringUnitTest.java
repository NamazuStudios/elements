package dev.getelements.elements.rt.guice;

import dev.getelements.elements.sdk.cluster.path.Path;
import dev.getelements.elements.rt.Resource;
import dev.getelements.elements.rt.ResourceService;
import dev.getelements.elements.rt.exception.ResourceNotFoundException;
import dev.getelements.elements.sdk.cluster.id.NodeId;
import dev.getelements.elements.sdk.cluster.id.ResourceId;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import jakarta.inject.Inject;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

import static dev.getelements.elements.sdk.cluster.id.ResourceId.randomResourceIdForNode;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.AssertJUnit.fail;

public abstract class AbstractResourceServiceAcquiringUnitTest {

    private final List<Object[]> intermediates = new CopyOnWriteArrayList<>();

    private final List<Object[]> linkedIntermediates = new CopyOnWriteArrayList<>();

    /**
     * Returns the actual {@link ResourceService} we need to fetch.
     * @return
     */
    public abstract ResourceService getResourceService();

    public Resource getMockResource(final ResourceId resourceId)  {
        final Resource resource = Mockito.mock(Resource.class);

        when(resource.getId()).thenReturn(resourceId);

        try {
            doAnswer(a -> {
                Assert.fail("No attempt to save resource should be made for this test.");
                return null;
            }).when(resource).serialize(any(OutputStream.class));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        try {
            doAnswer(a -> {
                Assert.fail("No attempt to save resource should be made for this test.");
                return null;
            }).when(resource).serialize(any(WritableByteChannel.class));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return resource;
    }

    @Inject
    private NodeId nodeId;

    @DataProvider
    public Object[][] initialDataProvider() {

        final List<Object[]> testData = new ArrayList<>();

        for (int i = 0; i < 100; ++i) {
            final ResourceId resourceId = randomResourceIdForNode(nodeId);
            final Path path = new Path(nodeId.asString(), asList("test", randomUUID().toString()));
            testData.add(new Object[]{resourceId, path});
        }

        return testData.toArray(new Object[][]{});

    }

    @Test(dataProvider = "initialDataProvider")
    public void testAdd(final ResourceId resourceId, final Path path) {

        final Resource resource = getMockResource(resourceId);

        getResourceService().addAndAcquireResource(path, resource);
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
            .map(ResourceService.Listing::getResourceId)
            .collect(toSet());

        final Set<Path> pathList = getResourceService().listStream(path)
            .map(ResourceService.Listing::getPath)
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

        final Set<ResourceId> resourceIdList = getResourceService().listStream(path)
                .map(l -> l.getResourceId())
                .collect(toSet());

        final Set<Path> pathList = getResourceService().listStream(path)
                .map(l -> l.getPath())
                .collect(toSet());

        assertEquals(resourceIdList, expectedResourceIdList);
        assertEquals(pathList, expectedPathList);

    }

    @Test(dependsOnMethods = "testAdd", dataProvider = "intermediateDataProvider")
    public void testGetResource(final ResourceId resourceId, final Path path, final Resource original) {
        try (final var txn = getResourceService().acquireWithTransaction(resourceId)) {
            assertEquals(txn.getResource().getId(), original.getId());
        }
    }

    @Test(dependsOnMethods = "testAdd", dataProvider = "intermediateDataProvider")
    public void testGetResourceAtPath(final ResourceId resourceId, final Path path, final Resource original) {
        try (final var txn = getResourceService().acquireWithTransaction(path)) {
            assertEquals(txn.getResource().getId(), original.getId());
        }
    }

    @Test(dataProvider = "initialDataProvider", expectedExceptions = ResourceNotFoundException.class)
    public void testGetResourceFail(final ResourceId resourceId, final Path path) {
        try (final var txn = getResourceService().acquireWithTransaction(resourceId)) {
            fail("Expected exception.");
        }
    }

    @Test(dataProvider = "initialDataProvider", expectedExceptions = ResourceNotFoundException.class)
    public void testGetResourceAtPathFail(final ResourceId resourceId, final Path path) {
        try (final var txn = getResourceService().acquireWithTransaction(path)) {
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
        try (var acquisition = getResourceService().acquire(path)) {
            assertEquals(acquisition.getResourceId(), original.getId());
        }
    }

    @Test(dependsOnMethods = {"testGetByAlias"}, dataProvider = "linkedIntermediateProvider", expectedExceptions = ResourceNotFoundException.class)
    public void testUnlink(final ResourceId resourceId, final Path path, final Resource original) {

        final ResourceService.Unlink unlink;
        unlink = getResourceService().unlinkPath(path, removed -> fail("Did not expect resource removal."));

        assertEquals(resourceId, unlink.getResourceId(), "Unlink mismatch");
        assertFalse(unlink.isRemoved(), "Resource should not have been removed.");

        try (var first = getResourceService().acquire(resourceId);
             var second = getResourceService().acquire(path)) {
            assertEquals(first.getResourceId(), original.getId());
            assertEquals(second.getResourceId(), original.getId());
        }

    }

    @Test(dependsOnMethods = {"testUnlink"}, dataProvider = "intermediateDataProvider")
    public void testRemove(final ResourceId resourceId, final Path path, final Resource original) {

        getResourceService().removeResource(resourceId);

        try (final var txn = getResourceService().acquireWithTransaction(path)) {
            fail("Resource still exists");
        } catch (ResourceNotFoundException ex) {
            // Pass Test
        }

        try (final var txn = getResourceService().acquireWithTransaction(resourceId)) {
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

        Stream<ResourceService.Listing> listingStream = getResourceService().listStream(new Path("*"));
        List<ResourceService.Listing> listingList = listingStream.collect(toList());
        assertEquals(listingList.size(), 0, "Expected empty dataset to start.");

        final ResourceId resourceId = randomResourceIdForNode(nodeId);
        final Resource resource = Mockito.mock(Resource.class);

        when(resource.getId()).thenReturn(resourceId);

        final Path path = new Path(randomUUID().toString());
        getResourceService().addAndAcquireResource(path, resource);

        final Path a = new Path(path, Path.fromComponents("a"));
        final Path b = new Path(path, Path.fromComponents("b"));
        getResourceService().link(resourceId, a);
        getResourceService().link(resourceId, b);

        getResourceService().destroy(resourceId);

        listingStream = getResourceService().listStream(new Path("*"));
        listingList = listingStream.collect(toList());
        assertEquals(listingList.size(), 0);

    }

}
