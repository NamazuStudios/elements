package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.dao.InventoryItemDao;
import com.namazustudios.socialengine.dao.ItemDao;
import com.namazustudios.socialengine.dao.RewardIssuanceDao;
import com.namazustudios.socialengine.dao.UserDao;
import com.namazustudios.socialengine.dao.mongo.model.goods.MongoInventoryItemId;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.goods.Item;
import com.namazustudios.socialengine.model.inventory.InventoryItem;
import com.namazustudios.socialengine.model.mission.RewardIssuance;
import com.namazustudios.socialengine.model.mission.Reward;
import com.namazustudios.socialengine.model.mission.Step;
import org.bson.types.ObjectId;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;

import static com.namazustudios.socialengine.model.User.Level.USER;
import static com.namazustudios.socialengine.model.mission.RewardIssuance.State.CREATED;
import static com.namazustudios.socialengine.model.mission.RewardIssuance.State.PENDING;
import static java.util.Arrays.asList;
import static java.util.Arrays.fill;
import static java.util.stream.Collectors.reducing;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.of;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

@Guice(modules = IntegrationTestModule.class)
public class MongoRewardIssuanceDaoTest {

    private UserDao userDao;

    private ItemDao itemDao;

    private InventoryItemDao inventoryItemDao;

    private RewardIssuanceDao rewardIssuanceDao;

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

        final RewardIssuance rewardIssuance = new RewardIssuance();
        final Reward reward = new Reward();
        final Step step = new Step();

        reward.setQuantity(10);
        reward.setItem(testItem);
        reward.addMetadata("foo", "bar");

        step.setCount(5);
        step.setDescription("Test");
        step.setRewards(asList(reward));
        step.setDisplayName("Test");

        rewardIssuance.setUser(testUser);
        rewardIssuance.setReward(reward);
        rewardIssuance.setState(CREATED);
        rewardIssuance.setStep(step);

        final RewardIssuance created = getRewardIssuanceDao().createRewardIssuance(rewardIssuance);
        assertNotNull(created.getId());
        assertEquals(created.getState(), CREATED);
        assertEquals(created.getReward(), reward);
        assertEquals(created.getStep(), step);

    }

    @DataProvider
    public Object[][] getCreatedPendingRewards() {
        final Object[][] objects = getRewardIssuanceDao()
            .getRewardIssuances(testUser, 0, 20, of(CREATED).collect(toSet()))
            .getObjects()
            .stream()
            .map(pr -> new Object[]{pr})
            .toArray(Object[][]::new);
        assertTrue(objects.length > 0);
        return objects;
    }

    @Test(dataProvider = "getCreatedPendingRewards", dependsOnMethods = "testCreatePendingReward")
    public void testFlagPending(final RewardIssuance rewardIssuance) {
        final RewardIssuance pending = getRewardIssuanceDao().flagPending(rewardIssuance);
        assertNotNull(pending.getId());
        assertEquals(pending.getState(), PENDING);
    }

    @DataProvider
    public Object[][] getPendingRewards() {
        final Object[][] objects = getRewardIssuanceDao()
            .getRewardIssuances(testUser, 0, 20, of(PENDING).collect(toSet()))
            .getObjects()
            .stream()
            .map(pr -> new Object[]{pr})
            .toArray(Object[][]::new);
        assertTrue(objects.length > 0);
        return objects;
    }

    @Test(dataProvider = "getPendingRewards", dependsOnMethods = "testFlagPending")
    public void testRedeem(final RewardIssuance rewardIssuance) {

        final String id = new MongoInventoryItemId(
                new ObjectId(testUser.getId()),
                new ObjectId(testItem.getId()),
                0).toHexString();

        int existing;

        try {
            existing = getInventoryItemDao().getInventoryItem(id).getQuantity();
        } catch (NotFoundException ex) {
            existing = 0;
        }


        final InventoryItem inventoryItem  = getRewardIssuanceDao().redeem(rewardIssuance);
        assertEquals(inventoryItem.getUser(), testUser);

        assertEquals(inventoryItem.getId(), id);
        assertEquals(inventoryItem.getUser(), testUser);
        assertEquals(inventoryItem.getItem(), testItem);
        assertEquals(inventoryItem.getPriority(), Integer.valueOf(0));
        assertEquals(inventoryItem.getQuantity(), Integer.valueOf(existing + rewardIssuance.getReward().getQuantity()));

        final InventoryItem repeatInventoryItem = getRewardIssuanceDao().redeem(rewardIssuance);
        assertEquals(repeatInventoryItem.getQuantity(), Integer.valueOf(existing + rewardIssuance.getReward().getQuantity()));

        final RewardIssuance postModified = getRewardIssuanceDao().getRewardIssuance(rewardIssuance.getId());
        assertEquals(postModified.getState(), RewardIssuance.State.REWARDED);
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

    public RewardIssuanceDao getRewardIssuanceDao() {
        return rewardIssuanceDao;
    }

    @Inject
    public void setRewardIssuanceDao(RewardIssuanceDao rewardIssuanceDao) {
        this.rewardIssuanceDao = rewardIssuanceDao;
    }

}
