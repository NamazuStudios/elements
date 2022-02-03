package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.dao.DistinctInventoryItemDao;
import com.namazustudios.socialengine.exception.inventory.DistinctInventoryItemNotFoundException;
import com.namazustudios.socialengine.model.goods.Item;
import com.namazustudios.socialengine.model.inventory.DistinctInventoryItem;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.util.PaginationWalker;
import org.bson.types.ObjectId;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.namazustudios.socialengine.model.goods.ItemCategory.DISTINCT;
import static java.util.Collections.unmodifiableList;
import static java.util.UUID.randomUUID;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

@Guice(modules = IntegrationTestModule.class)
public class MongoDistinctInventorItemDaoTest {

    private static final int ITEM_COUNT = 5;

    private static final int USER_COUNT = 5;

    private static final int PROFILE_COUNT = 5;

    @Inject
    private DistinctInventoryItemDao underTest;

    @Inject
    private ItemTestFactory itemTestFactory;

    @Inject
    private UserTestFactory userTestFactory;

    @Inject
    private ProfileTestFactory profileTestFactory;

    @Inject
    private ApplicationTestFactory applicationTestFactory;

    private List<Item> itemList;

    private List<User> userList;

    private List<Profile> profileList;

    private final Map<Object, DistinctInventoryItem> intermediates = new ConcurrentHashMap<>();

    @BeforeClass
    public void setup() {

        var itemList = new ArrayList<Item>();
        var userList = new ArrayList<User>();
        var profileList = new ArrayList<Profile>();

        final var application = applicationTestFactory.createMockApplication("Distinct Items");

        for (int i = 0; i < ITEM_COUNT; ++i) {
            final var item = itemTestFactory.createTestItem(DISTINCT);
            itemList.add(item);
        }

        for (int i = 0; i < USER_COUNT; ++i) {

            final var user = userTestFactory.createTestUser();
            userList.add(user);

            for (int j = 0; j < PROFILE_COUNT; ++j) {
                final var profile = profileTestFactory.makeMockProfile(user, application);
                profileList.add(profile);
            }

        }

        // Immutable for thread safety
        this.itemList = unmodifiableList(itemList);
        this.userList = unmodifiableList(userList);
        this.profileList = unmodifiableList(profileList);

    }

    @DataProvider
    public Object[][] getIntermediates() {
        return intermediates
                .entrySet()
                .stream()
                .map(e -> new Object[]{e.getKey(), e.getValue()})
                .toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] getDistinctItemsToCreate() {

        final var output = new ArrayList<Object[]>();

        for (var item : itemList) {

            // User items with no metadata
            for (var user : userList) {
                final var toCreate = new DistinctInventoryItem();
                toCreate.setUser(user);
                toCreate.setItem(item);
                output.add(new Object[]{toCreate});
            }

            // User items with metadata
            for (var user : userList) {
                final var toCreate = new DistinctInventoryItem();
                toCreate.setUser(user);
                toCreate.setItem(item);
                toCreate.setMetadata(generateMockMetadata());
                output.add(new Object[]{toCreate});
            }

            // Profile items with no metadata
            for (var profile : profileList) {
                final var toCreate = new DistinctInventoryItem();
                toCreate.setProfile(profile);
                toCreate.setUser(profile.getUser());
                toCreate.setItem(item);
                output.add(new Object[]{toCreate});
            }

            // Profile items with metadata
            for (var profile : profileList) {
                final var toCreate = new DistinctInventoryItem();
                toCreate.setProfile(profile);
                toCreate.setUser(profile.getUser());
                toCreate.setItem(item);
                toCreate.setMetadata(generateMockMetadata());
                output.add(new Object[]{toCreate});
            }

        }

        return output.toArray(Object[][]::new);

    }

    private void saveIntermediate(final DistinctInventoryItem item) {
        intermediates.put(item.getId(), item);
    }

    private Map<String, Object> generateMockMetadata() {
        return Map.of(
                randomUUID().toString(), randomUUID().toString(),
                randomUUID().toString(), randomUUID().toString(),
                randomUUID().toString(), randomUUID().toString(),
                randomUUID().toString(), randomUUID().toString()
        );
    }

    @Test(expectedExceptions = DistinctInventoryItemNotFoundException.class)
    public void testGetNonsense() {
        underTest.getDistinctInventoryItem("asdf");
    }

    @Test(expectedExceptions = DistinctInventoryItemNotFoundException.class)
    public void testGetNotFound() {
        underTest.getDistinctInventoryItem(new ObjectId().toHexString());
    }

    @Test(dataProvider = "getDistinctItemsToCreate")
    public void testCreateDistinctUserInventoryItem(final DistinctInventoryItem toCreate) {
        final var created = underTest.createDistinctInventoryItem(toCreate);
        assertNotNull(created.getId());
        assertEquals(created.getItem(), toCreate.getItem());
        assertEquals(created.getUser(), toCreate.getUser());
        assertEquals(created.getMetadata(), toCreate.getMetadata());
        saveIntermediate(created);
    }

    @Test(dependsOnMethods = "testCreateDistinctUserInventoryItem")
    public void testGetAllItems() {
        final var all = new PaginationWalker().toList((offset, count) ->
            underTest.getDistinctInventoryItems(offset, count, null, null)
        );
        intermediates.values().containsAll(all);
    }

    @Test(dataProvider = "getIntermediates", dependsOnMethods = "testCreateDistinctUserInventoryItem")
    public void testGetSingleDistinctInventoryItem(final String owner, final DistinctInventoryItem item) {
        final var fetched = underTest.getDistinctInventoryItem(item.getId());
        assertEquals(fetched, item);
    }

    @Test(dataProvider = "getIntermediates",
            dependsOnMethods = {
                    "testGetAllItems",
                    "testGetSingleDistinctInventoryItem"
            })
    public void testUpdate(final String owner, final DistinctInventoryItem item) {
        final var toUpdate = new DistinctInventoryItem();
        toUpdate.setId(item.getId());
        toUpdate.setUser(item.getUser());
        toUpdate.setItem(item.getItem());
        toUpdate.setProfile(item.getProfile());

        final Map<String, Object> metadata;

        if (toUpdate.getMetadata() == null) {
            metadata = generateMockMetadata();
        } else {
            metadata = null;
        }

        toUpdate.setMetadata(metadata);

        final var updated = underTest.updateDistinctInventoryItem(toUpdate);
        assertEquals(updated.getItem(), toUpdate.getItem());
        assertEquals(updated.getUser(), toUpdate.getUser());
        assertEquals(updated.getProfile(), toUpdate.getProfile());
        assertEquals(updated.getMetadata(), metadata);
        saveIntermediate(updated);

    }

    @Test(dataProvider = "getIntermediates",
            dependsOnMethods = {"testUpdate"})
    public void testDelete(final String owner, final DistinctInventoryItem item) {
        underTest.deleteDistinctInventoryItem(item.getId());
    }

    @Test(dataProvider = "getIntermediates",
          dependsOnMethods = {"testDelete"},
          expectedExceptions = DistinctInventoryItemNotFoundException.class)
    public void testDoubleDelete(final String owner, final DistinctInventoryItem item) {
        underTest.deleteDistinctInventoryItem(item.getId());
    }

    @Test(dataProvider = "getIntermediates",
          dependsOnMethods = {"testDelete"},
          expectedExceptions = DistinctInventoryItemNotFoundException.class)
    public void testFetchPostDelete(final String owner, final DistinctInventoryItem item) {
        underTest.getDistinctInventoryItem(item.getId());
    }

}
