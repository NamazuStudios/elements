package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.dao.InventoryItemDao;
import com.namazustudios.socialengine.dao.ItemDao;
import com.namazustudios.socialengine.dao.PendingRewardDao;
import com.namazustudios.socialengine.dao.UserDao;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.goods.Item;
import com.namazustudios.socialengine.model.inventory.InventoryItem;
import com.namazustudios.socialengine.model.mission.PendingReward;
import com.namazustudios.socialengine.model.mission.Reward;
import com.namazustudios.socialengine.model.mission.Step;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;

import static com.namazustudios.socialengine.model.User.Level.USER;
import static com.namazustudios.socialengine.model.mission.PendingReward.State.CREATED;
import static com.namazustudios.socialengine.model.mission.PendingReward.State.PENDING;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.of;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

@Guice(modules = IntegrationTestModule.class)
public class MongoPendingRewardDaoTest {

    private UserDao userDao;

    private ItemDao itemDao;

    private InventoryItemDao inventoryItemDao;

    private PendingRewardDao pendingRewardDao;

    private User testUser;

    private Item testItem;

    @BeforeClass
    public void createTestItems() {

        testUser = new User();
        testUser.setName("testy.mctesterson.5");
        testUser.setEmail("testy.mctesterson.5@example.com");
        testUser.setLevel(USER);

        testItem = new Item();
        testItem.setName("item_d");
        testItem.setDisplayName("Test Item D.");
        testItem.setDescription("A simple test item.");
        testItem.setTags(of("a").collect(toSet()));
        testItem.addMetadata("key", "a");

        testItem = getItemDao().createItem(testItem);
        testUser = getUserDao().createOrReactivateUser(testUser);
        testUser = getUserDao().createOrReactivateUser(testUser);

    }

    @Test(invocationCount = 10)
    public void testCreatePendingReward() {

        final PendingReward pendingReward = new PendingReward();
        final Reward reward = new Reward();
        final Step step = new Step();

        reward.setQuantity(10);
        reward.setItem(testItem);
        reward.addMetadata("foo", "bar");

        step.setCount(5);
        step.setDescription("Test");
        step.setRewards(asList(reward));
        step.setDisplayName("Test");

        pendingReward.setUser(testUser);
        pendingReward.setReward(reward);
        pendingReward.setState(CREATED);
        pendingReward.setStep(step);

        final PendingReward created = getPendingRewardDao().createPendingReward(pendingReward);
        assertNotNull(created.getId());
        assertEquals(created.getState(), CREATED);
        assertEquals(created.getReward(), reward);
        assertEquals(created.getStep(), step);

    }

    @DataProvider
    public Object[][] getCreatedPendingRewards() {
        final Object[][] objects = getPendingRewardDao()
            .getPendingRewards(testUser, 0, 20, of(CREATED).collect(toSet()))
            .getObjects()
            .stream()
            .map(pr -> new Object[]{pr})
            .toArray(Object[][]::new);
        assertTrue(objects.length > 0);
        return objects;
    }

    @Test(dataProvider = "getCreatedPendingRewards", dependsOnMethods = "testCreatePendingReward")
    public void testFlagPending(final PendingReward pendingReward) {
        final PendingReward pending = getPendingRewardDao().flagPending(pendingReward);
        assertNotNull(pending.getId());
        assertEquals(pending.getState(), PENDING);
    }

    @DataProvider
    public Object[][] getPendingRewards() {
        final Object[][] objects = getPendingRewardDao()
            .getPendingRewards(testUser, 0, 20, of(PENDING).collect(toSet()))
            .getObjects()
            .stream()
            .map(pr -> new Object[]{pr})
            .toArray(Object[][]::new);
        assertTrue(objects.length > 0);
        return objects;
    }

    @Test(dataProvider = "getPendingRewards", dependsOnMethods = "testFlagPending")
    public void testRedeem(final PendingReward pendingReward) {
        final InventoryItem before = getInventoryItemDao().adjustQuantityForItem(testUser, testItem.getId(), 0, 0);
        assertEquals(before.getUser(), testUser);

        final InventoryItem after  = getPendingRewardDao().redeem(pendingReward);
        assertEquals(after.getUser(), testUser);

        assertEquals(after.getId(), before.getId());
        assertEquals(after.getUser(), before.getUser());
        assertEquals(after.getItem(), testItem);
        assertEquals(after.getPriority(), Integer.valueOf(0));
        assertEquals(after.getQuantity(), Integer.valueOf(before.getQuantity() + pendingReward.getReward().getQuantity()));

    }

    public UserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public ItemDao getItemDao() {
        return itemDao;
    }

    @Inject
    public void setItemDao(ItemDao itemDao) {
        this.itemDao = itemDao;
    }

    public InventoryItemDao getInventoryItemDao() {
        return inventoryItemDao;
    }

    @Inject
    public void setInventoryItemDao(InventoryItemDao inventoryItemDao) {
        this.inventoryItemDao = inventoryItemDao;
    }

    public PendingRewardDao getPendingRewardDao() {
        return pendingRewardDao;
    }

    @Inject
    public void setPendingRewardDao(PendingRewardDao pendingRewardDao) {
        this.pendingRewardDao = pendingRewardDao;
    }

}
