package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.dao.ItemDao;
import com.namazustudios.socialengine.model.goods.Item;
import com.namazustudios.socialengine.model.inventory.CreateDistinctInventoryItemRequest;
import com.namazustudios.socialengine.model.inventory.DistinctInventoryItem;
import com.namazustudios.socialengine.model.inventory.UpdateDistinctInventoryItemRequest;
import com.namazustudios.socialengine.rest.model.DistinctInventoryItemPagination;
import com.namazustudios.socialengine.util.PaginationWalker;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static com.namazustudios.socialengine.model.goods.ItemCategory.DISTINCT;
import static com.namazustudios.socialengine.rest.TestUtils.TEST_API_ROOT;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toSet;
import static org.testng.AssertJUnit.*;

public class SuperUserDistinctInventoryItemApiTest {

    @Factory
    public Object[] getTests() {
        return new Object[] {
            TestUtils.getInstance().getXodusTest(SuperUserDistinctInventoryItemApiTest.class),
            TestUtils.getInstance().getUnixFSTest(SuperUserDistinctInventoryItemApiTest.class)
        };
    }

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private Client client;

    @Inject
    private ClientContext userClientContextA;

    @Inject
    private ClientContext userClientContextB;

    @Inject
    private ClientContext superUserClientContext;

    @Inject
    private ItemDao itemDao;

    private Item item;

    private final Map<String, Set<DistinctInventoryItem>> intermediatesByUser = new ConcurrentHashMap<>();

    private final Map<String, Set<DistinctInventoryItem>> intermediatesByProfile = new ConcurrentHashMap<>();

    @BeforeClass
    public void setup() {

        superUserClientContext
                .createSuperuser("distinct")
                .createProfile("DistinctA")
                .createSession();

        userClientContextA
                .createUser("distincta")
                .createProfile("DistinctA")
                .createSession();

        userClientContextB
                .createUser("distinctb")
                .createProfile("DistinctB")
                .createSession();

        final var item = new Item();
        item.setName("distinct1");
        item.setCategory(DISTINCT);
        item.setDescription("Test Item");
        item.setDisplayName("Test Item");
        this.item = itemDao.createItem(item);

    }

    private Map<String, Object> generateMockMetadata() {
        return Map.of(
                randomUUID().toString(), randomUUID().toString(),
                randomUUID().toString(), randomUUID().toString(),
                randomUUID().toString(), randomUUID().toString(),
                randomUUID().toString(), randomUUID().toString()
        );
    }

    private void updateIntermediate(final DistinctInventoryItem distinctInventoryItem) {

        final Set<DistinctInventoryItem> itemSet;

        if (distinctInventoryItem.getProfile() != null) {
            final var id = distinctInventoryItem.getProfile().getId();
            itemSet = intermediatesByProfile.computeIfAbsent(id, i -> ConcurrentHashMap.newKeySet());
        } else if (distinctInventoryItem.getUser() != null) {
            final var id = distinctInventoryItem.getUser().getId();
            itemSet = intermediatesByUser.computeIfAbsent(id, i -> ConcurrentHashMap.newKeySet());
        } else {
            throw new IllegalArgumentException("must contain either user or profile");
        }

        itemSet.add(distinctInventoryItem);

    }

    @DataProvider
    public Object[][] getUserClientContexts() {
        return new Object[][] {
                new Object[] {userClientContextA},
                new Object[] {userClientContextB}
        };
    }

    @DataProvider
    public Object[][] getAllIntermediates() {
        return Stream.of(intermediatesByUser, intermediatesByProfile)
            .flatMap(m -> m.values().stream())
            .flatMap(s -> s.stream().map(DistinctInventoryItem::getId))
            .collect(toSet())
            .stream()
            .map(s -> new Object[]{s})
            .toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] getUserIntermediates() {
        return Stream.of(userClientContextA, userClientContextB)
            .flatMap(c -> intermediatesByUser.get(c.getUser().getId()).stream().map(i -> new Object[] {c, i}))
            .toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] getProfileIntermediates() {
        return Stream.of(userClientContextA, userClientContextB)
            .flatMap(c -> intermediatesByProfile.get(c.getDefaultProfile().getId()).stream().map(i -> new Object[] {c, i}))
            .toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] getNonMatchingUserIntermediates() {
        return Stream.of(userClientContextA, userClientContextB)
                .flatMap(c -> intermediatesByProfile
                        .values()
                        .stream()
                        .flatMap(s -> s.stream().filter(i -> !i.getUser().getId().equals(c.getUser().getId())))
                        .map(i -> new Object[]{c, i}))
                .toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] getNonMatchingProfileIntermediates() {
        return Stream.of(userClientContextA, userClientContextB)
                .flatMap(c -> intermediatesByProfile
                        .values()
                        .stream()
                        .flatMap(s -> s.stream().filter(i -> !i.getUser().getId().equals(c.getUser().getId())))
                        .map(i -> new Object[]{c, i}))
                .toArray(Object[][]::new);
    }

    @Test(invocationCount = 50, threadPoolSize = 10)
    public void getSingleNotFound() {

        final var response = client
                .target(format("%s/inventory/distinct/%s", apiRoot, randomUUID()))
                .request()
                .header("Authorization", format("Bearer %s", superUserClientContext.getSessionSecret()))
                .get();

        assertEquals(404, response.getStatus());

    }

    @Test(threadPoolSize = 5, dataProvider = "getUserClientContexts")
    public void testCreateItemUser(final ClientContext userClientContext) {

        var request = new CreateDistinctInventoryItemRequest();
        request.setItemId(item.getId());
        request.setUserId(userClientContext.getUser().getId());

        final var response = client
                .target(format("%s/inventory/distinct", apiRoot))
                .request()
                .header("Authorization", format("Bearer %s", superUserClientContext.getSessionSecret()))
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        assertEquals(200, response.getStatus());

        final var created = response.readEntity(DistinctInventoryItem.class);
        assertNotNull(created.getId());
        assertNull(created.getProfile());
        assertEquals(userClientContext.getUser(), created.getUser());
        updateIntermediate(created);

    }

    @Test(threadPoolSize = 5, dataProvider = "getUserClientContexts")
    public void testCreateItemProfile(final ClientContext userClientContext) {

        var request = new CreateDistinctInventoryItemRequest();
        request.setItemId(item.getId());
        request.setUserId(userClientContext.getUser().getId());
        request.setProfileId(userClientContext.getDefaultProfile().getId());

        final var response = client
            .target(format("%s/inventory/distinct", apiRoot))
            .request()
            .header("Authorization", format("Bearer %s", superUserClientContext.getSessionSecret()))
            .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        assertEquals(200, response.getStatus());

        final var created = response.readEntity(DistinctInventoryItem.class);
        assertNotNull(created.getId());
        assertEquals(userClientContext.getUser(), created.getUser());
        assertEquals(userClientContext.getDefaultProfile(), created.getProfile());
        updateIntermediate(created);

    }


    @Test(dataProvider = "getUserIntermediates", dependsOnMethods = "testCreateItemUser")
    public void testPutUserItem(final ClientContext clientContext,
                                final DistinctInventoryItem distinctInventoryItem) {

        var request = new UpdateDistinctInventoryItemRequest();
        request.setUserId(clientContext.getUser().getId());
        request.setMetadata(generateMockMetadata());

        final var response = client
            .target(format("%s/inventory/distinct/%s", apiRoot, distinctInventoryItem.getId()))
            .request()
            .header("Authorization", format("Bearer %s", superUserClientContext.getSessionSecret()))
            .put(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        assertEquals(200, response.getStatus());

        final var updated = response.readEntity(DistinctInventoryItem.class);
        assertEquals(distinctInventoryItem.getId(), updated.getId());
        assertEquals(distinctInventoryItem.getUser(), updated.getUser());
        assertEquals(distinctInventoryItem.getProfile(), updated.getProfile());
        assertEquals(request.getMetadata(), updated.getMetadata());
        updateIntermediate(updated);

    }

    @Test(dataProvider = "getProfileIntermediates", dependsOnMethods = "testCreateItemProfile")
    public void testPutProfileItem(final ClientContext clientContext,
                                   final DistinctInventoryItem distinctInventoryItem) {

        var request = new UpdateDistinctInventoryItemRequest();
        request.setUserId(clientContext.getUser().getId());
        request.setMetadata(generateMockMetadata());

        final var response = client
                .target(format("%s/inventory/distinct/%s", apiRoot, distinctInventoryItem.getId()))
                .request()
                .header("Authorization", format("Bearer %s", superUserClientContext.getSessionSecret()))
                .put(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        assertEquals(200, response.getStatus());

        final var updated = response.readEntity(DistinctInventoryItem.class);
        assertEquals(distinctInventoryItem.getId(), updated.getId());
        assertEquals(distinctInventoryItem.getUser(), updated.getUser());
        assertEquals(distinctInventoryItem.getProfile(), updated.getProfile());
        assertEquals(request.getMetadata(), updated.getMetadata());
        updateIntermediate(updated);

    }

    @Test(dataProvider = "getUserClientContexts", dependsOnMethods = {
        "testCreateItemUser",
        "testCreateItemProfile"
    })
    public void testGetAllSpecifyingUser(final ClientContext userClientContext) {

        final PaginationWalker.WalkFunction<DistinctInventoryItem> walkFunction = (offset, count) -> client
            .target(format("%s/inventory/distinct?offset=%d&count=%d&userId=%s",
                apiRoot,
                offset,
                count,
                userClientContext.getUser().getId())
            )
            .request()
            .header("Authorization", format("Bearer %s", superUserClientContext.getSessionSecret()))
            .get(DistinctInventoryItemPagination.class);

        new PaginationWalker().forEach(walkFunction, i -> assertEquals(
                userClientContext.getUser().getId(),
                i.getUser().getId()
            )
        );

    }

    @Test(dataProvider = "getUserClientContexts", dependsOnMethods = {
            "testCreateItemUser",
            "testCreateItemProfile"
    })
    public void testGetAllSpecifyingProfile(final ClientContext userClientContext) {

        final PaginationWalker.WalkFunction<DistinctInventoryItem> walkFunction = (offset, count) -> client
                .target(format("%s/inventory/distinct?offset=%d&count=%d&profileId=%s",
                        apiRoot,
                        offset,
                        count,
                        userClientContext.getDefaultProfile().getId())
                )
                .request()
                .header("Authorization", format("Bearer %s", superUserClientContext.getSessionSecret()))
                .get(DistinctInventoryItemPagination.class);

        new PaginationWalker().forEach(walkFunction, i -> assertEquals(
                userClientContext.getUser().getId(),
                i.getUser().getId()
            )
        );

    }

    @Test(dataProvider = "getUserIntermediates", dependsOnMethods = {
            "testCreateItemUser",
            "testCreateItemProfile"
    })
    public void testUserGetSingle(final ClientContext userClientContext,
                                  final DistinctInventoryItem distinctInventoryItem) {

        final var item = client
                .target(format("%s/inventory/distinct/%s", apiRoot, distinctInventoryItem.getId()))
                .request()
                .header("Authorization", format("Bearer %s", superUserClientContext.getSessionSecret()))
                .get(DistinctInventoryItem.class);

        assertEquals(userClientContext.getUser().getId(), item.getUser().getId());

    }

    @Test(dataProvider = "getProfileIntermediates", dependsOnMethods = {
            "testCreateItemUser",
            "testCreateItemProfile"
    })
    public void testProfileGetSingle(final ClientContext userClientContext,
                                     final DistinctInventoryItem distinctInventoryItem) {

        final var item = client
                .target(format("%s/inventory/distinct/%s", apiRoot, distinctInventoryItem.getId()))
                .request()
                .header("Authorization", format("Bearer %s", superUserClientContext.getSessionSecret()))
                .get(DistinctInventoryItem.class);

        assertEquals(userClientContext.getDefaultProfile().getId(), item.getProfile().getId());

    }

    @Test(dataProvider = "getAllIntermediates", dependsOnMethods = {
        "testGetAllSpecifyingUser",
        "testGetAllSpecifyingProfile",
        "testUserGetSingle",
        "testProfileGetSingle"
    })
    public void testDeleteItem(final String distinctInventoryItemId) {

        final var response = client
            .target(format("%s/inventory/distinct/%s", apiRoot, distinctInventoryItemId))
            .request()
            .header("Authorization", format("Bearer %s", superUserClientContext.getSessionSecret()))
            .delete();

        assertEquals(204, response.getStatus());

    }

    @Test(dataProvider = "getAllIntermediates", dependsOnMethods = "testDeleteItem")
    public void testDeletedItemThrows404(final String distinctInventoryItemId) {

        final var response = client
            .target(format("%s/inventory/distinct/%s", apiRoot, distinctInventoryItemId))
            .request()
            .header("Authorization", format("Bearer %s", superUserClientContext.getSessionSecret()))
            .delete();

        assertEquals(404, response.getStatus());

    }


    @Test(dataProvider = "getAllIntermediates", dependsOnMethods = "testDeletedItemThrows404")
    public void testItemsAreDeleted(final String distinctInventoryItemId) {

        final var response = client
                .target(format("%s/inventory/distinct/%s", apiRoot, distinctInventoryItemId))
                .request()
                .header("Authorization", format("Bearer %s", superUserClientContext.getSessionSecret()))
                .get();

        assertEquals(404, response.getStatus());

    }

}
