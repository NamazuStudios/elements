package dev.getelements.elements.rest;

import dev.getelements.elements.dao.DistinctInventoryItemDao;
import dev.getelements.elements.dao.ItemDao;
import dev.getelements.elements.model.goods.Item;
import dev.getelements.elements.model.inventory.CreateDistinctInventoryItemRequest;
import dev.getelements.elements.model.inventory.DistinctInventoryItem;
import dev.getelements.elements.model.inventory.UpdateDistinctInventoryItemRequest;
import dev.getelements.elements.rest.model.DistinctInventoryItemPagination;
import dev.getelements.elements.util.PaginationWalker;
import dev.getelements.elements.util.PaginationWalker.WalkFunction;
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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static dev.getelements.elements.model.goods.ItemCategory.DISTINCT;
import static dev.getelements.elements.rest.TestUtils.TEST_API_ROOT;
import static io.smallrye.common.constraint.Assert.assertNotNull;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static org.testng.AssertJUnit.assertEquals;

public class UserDistinctInventoryItemApiTest {

    @Factory
    public Object[] getTests() {
        return new Object[] {
                TestUtils.getInstance().getXodusTest(UserDistinctInventoryItemApiTest.class),
                TestUtils.getInstance().getUnixFSTest(UserDistinctInventoryItemApiTest.class)
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
    private ClientContext userWithPublicItems;

    @Inject
    private ItemDao itemDao;

    @Inject
    private DistinctInventoryItemDao distinctInventoryItemDao;

    private Item item;

    private Item publicItem;

    private final Map<String, Set<DistinctInventoryItem>> intermediatesByUser = new ConcurrentHashMap<>();

    private final Map<String, Set<DistinctInventoryItem>> intermediatesByProfile = new ConcurrentHashMap<>();

    @BeforeClass
    public void setup() {

        userClientContextA
                .createUser("distincta")
                .createProfile("DistinctA")
                .createSession();

        userClientContextB
                .createUser("distinctb")
                .createProfile("DistinctB")
                .createSession();

        userWithPublicItems
                .createUser("distinctPublic")
                .createProfile("DistinctPublic")
                .createSession();

        final var item = new Item();
        item.setName("distinct0");
        item.setCategory(DISTINCT);
        item.setDescription("Test Item");
        item.setDisplayName("Test Item");
        this.item = itemDao.createItem(item);

        final var publicItem = new Item();
        publicItem.setName("publicDistinct" + UUID.randomUUID().toString().replaceAll("-", ""));
        publicItem.setCategory(DISTINCT);
        publicItem.setDescription("Public test item");
        publicItem.setDisplayName("Public test item");
        publicItem.setPublicVisible(true);
        this.publicItem = itemDao.createItem(publicItem);

        for (int i = 0; i < 10; ++i) {
            makeDistinctItems(userClientContextA, false);
            makeDistinctItems(userClientContextB, false);
            makeDistinctItems(userWithPublicItems, true);
        }

    }

    private void makeDistinctItems(final ClientContext userClientContext, final boolean isPublic) {

        // User Scoped
        var distinct = new DistinctInventoryItem();
        distinct.setItem(isPublic ? publicItem : item);
        distinct.setUser(userClientContext.getUser());
        distinct = distinctInventoryItemDao.createDistinctInventoryItem(distinct);
        updateIntermediate(distinct);

        // Profile Scoped
        distinct = new DistinctInventoryItem();
        distinct.setItem(isPublic ? publicItem : item);
        distinct.setUser(userClientContext.getUser());
        distinct.setProfile(userClientContext.getDefaultProfile());
        distinct = distinctInventoryItemDao.createDistinctInventoryItem(distinct);
        updateIntermediate(distinct);

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

    @Test(invocationCount = 50, threadPoolSize = 10, dataProvider = "getUserClientContexts")
    public void getSingleNotFound(final ClientContext userClientContext) {

        final var response = client
                .target(format("%s/inventory/distinct/%s", apiRoot, randomUUID()))
                .request()
                .header("Authorization", format("Bearer %s", userClientContext.getSessionSecret()))
                .get();

        assertEquals(404, response.getStatus());

    }

    @Test(invocationCount = 50, threadPoolSize = 10, dataProvider = "getUserClientContexts")
    public void testPostIsForbidden(final ClientContext userClientContext) {

        var request = new CreateDistinctInventoryItemRequest();
        request.setItemId(item.getId());
        request.setUserId(userClientContext.getUser().getId());
        request.setProfileId(userClientContext.getDefaultProfile().getId());

        final var response = client
                .target(format("%s/inventory/distinct", apiRoot))
                .request()
                .header("Authorization", format("Bearer %s", userClientContext.getSessionSecret()))
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        assertEquals(403, response.getStatus());

    }


    @Test(invocationCount = 50, threadPoolSize = 10, dataProvider = "getUserClientContexts")
    public void testPutIsForbidden(final ClientContext userClientContext) {

        var request = new UpdateDistinctInventoryItemRequest();
        request.setUserId(userClientContext.getUser().getId());
        request.setProfileId(userClientContext.getDefaultProfile().getId());

        final var response = client
                .target(format("%s/inventory/distinct/foo", apiRoot))
                .request()
                .header("Authorization", format("Bearer %s", userClientContext.getSessionSecret()))
                .put(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        assertEquals(403, response.getStatus());

    }

    @Test(dataProvider = "getUserClientContexts")
    public void testGetAllUser(final ClientContext userClientContext) {

        final WalkFunction<DistinctInventoryItem> walkFunction = (offset, count) -> client
                .target(format("%s/inventory/distinct?offset=%d&count=%d", apiRoot, offset, count))
                .request()
                .header("Authorization", format("Bearer %s", userClientContext.getSessionSecret()))
                .get(DistinctInventoryItemPagination.class);

        new PaginationWalker().forEach(walkFunction, i -> assertEquals(
                        userClientContext.getUser().getId(),
                        i.getUser().getId()
                )
        );

    }

    @Test(dataProvider = "getUserClientContexts")
    public void testGetAllUserSpecifyingUser(final ClientContext userClientContext) {

        final WalkFunction<DistinctInventoryItem> walkFunction = (offset, count) -> client
                .target(format("%s/inventory/distinct?offset=%d&count=%d&userId=%s",
                        apiRoot,
                        offset,
                        count,
                        userClientContext.getUser().getId())
                )
                .request()
                .header("Authorization", format("Bearer %s", userClientContext.getSessionSecret()))
                .get(DistinctInventoryItemPagination.class);

        new PaginationWalker().forEach(walkFunction, i -> assertEquals(
                        userClientContext.getUser().getId(),
                        i.getUser().getId()
                )
        );

    }

    @Test(dataProvider = "getUserClientContexts")
    public void testGetAllSpecifyingOtherUsersPublicInventory(final ClientContext userClientContext) {
        final PaginationWalker.WalkFunction<DistinctInventoryItem> walkFunction = (offset, count) -> client
                .target(format("%s/inventory/distinct?offset=%d&count=%d&userId=%s",
                        apiRoot,
                        offset,
                        count,
                        userWithPublicItems.getUser().getId())
                )
                .request()
                .header("Authorization", format("Bearer %s", userClientContext.getSessionSecret()))
                .get(DistinctInventoryItemPagination.class);

        new PaginationWalker().forEach(walkFunction, i -> assertEquals(
                        userClientContext.getUser().getId(),
                        i.getUser().getId()
                )
        );

    }

    @Test(dataProvider = "getUserClientContexts")
    public void testGetAllProfile(final ClientContext userClientContext) {

        final WalkFunction<DistinctInventoryItem> walkFunction = (offset, count) -> client
                .target(format("%s/inventory/distinct?offset=%d&count=%d&profileId=%s",
                        apiRoot,
                        offset,
                        count,
                        userClientContext.getDefaultProfile().getId())
                )
                .request()
                .header("Authorization", format("Bearer %s", userClientContext.getSessionSecret()))
                .get(DistinctInventoryItemPagination.class);


        new PaginationWalker().forEach(walkFunction, i -> {
            assertEquals(userClientContext.getUser().getId(), i.getUser().getId());
            assertNotNull(i.getProfile().getImageObject().getUrl());
            }
        );

    }

    @Test(dataProvider = "getUserIntermediates")
    public void testUserGetSingle(final ClientContext userClientContext,
                                  final DistinctInventoryItem distinctInventoryItem) {

        final var item = client
                .target(format("%s/inventory/distinct/%s", apiRoot, distinctInventoryItem.getId()))
                .request()
                .header("Authorization", format("Bearer %s", userClientContext.getSessionSecret()))
                .get(DistinctInventoryItem.class);

        assertEquals(userClientContext.getUser().getId(), item.getUser().getId());

    }

    @Test(dataProvider = "getProfileIntermediates")
    public void testProfileGetSingle(final ClientContext userClientContext,
                                     final DistinctInventoryItem distinctInventoryItem) {

        final var item = client
                .target(format("%s/inventory/distinct/%s", apiRoot, distinctInventoryItem.getId()))
                .request()
                .header("Authorization", format("Bearer %s", userClientContext.getSessionSecret()))
                .get(DistinctInventoryItem.class);

        assertEquals(userClientContext.getDefaultProfile().getId(), item.getProfile().getId());
        assertNotNull(item.getProfile().getImageObject().getUrl());
    }

    @Test(dataProvider = "getNonMatchingUserIntermediates")
    public void testUserGetSingleFailsNonMatching(
            final ClientContext userClientContext,
            final DistinctInventoryItem distinctInventoryItem) {

        final var response = client
                .target(format("%s/inventory/distinct/%s", apiRoot, distinctInventoryItem.getId()))
                .request()
                .header("Authorization", format("Bearer %s", userClientContext.getSessionSecret()))
                .get();

        assertEquals(404, response.getStatus());

    }


    @Test(dataProvider = "getNonMatchingProfileIntermediates")
    public void testProfileGetSingleFailsNonMatching(
            final ClientContext userClientContext,
            final DistinctInventoryItem distinctInventoryItem) {

        final var response = client
                .target(format("%s/inventory/distinct/%s", apiRoot, distinctInventoryItem.getId()))
                .request()
                .header("Authorization", format("Bearer %s", userClientContext.getSessionSecret()))
                .get();

        assertEquals(404, response.getStatus());

    }

}
