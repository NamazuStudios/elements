package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.dao.*;
import com.namazustudios.socialengine.dao.mongo.model.goods.MongoInventoryItemId;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.goods.Item;
import com.namazustudios.socialengine.model.inventory.InventoryItem;
import com.namazustudios.socialengine.model.mission.RewardIssuance;
import static com.namazustudios.socialengine.model.mission.RewardIssuance.State;
import static com.namazustudios.socialengine.model.mission.RewardIssuance.Type;
import com.namazustudios.socialengine.model.mission.Reward;
import com.namazustudios.socialengine.model.mission.Step;
import org.bson.types.ObjectId;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.namazustudios.socialengine.model.User.Level.USER;
import static com.namazustudios.socialengine.model.mission.RewardIssuance.State.*;
import static com.namazustudios.socialengine.model.mission.RewardIssuance.Type.*;
import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.asList;
import static java.util.Arrays.fill;
import static java.util.stream.Collectors.reducing;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.of;
import static org.testng.Assert.*;

@Guice(modules = IntegrationTestModule.class)
public class MongoRewardIssuanceDaoTest {
    private static final int INVOCATION_COUNT = 10;

    private UserDao userDao;

    private ItemDao itemDao;

    private InventoryItemDao inventoryItemDao;

    private RewardIssuanceDao rewardIssuanceDao;

    private RewardDao rewardDao;

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

    @Test()
    public void testCreateExpiringRewardIssuance(ITestContext testContext) {
        final Reward reward = new Reward();

        reward.setQuantity(5);
        reward.setItem(testItem);
        reward.addMetadata("foo", "bar");

        final Reward createdReward = getRewardDao().createReward(reward);

        final RewardIssuance rewardIssuance = new RewardIssuance();

        rewardIssuance.setUser(testUser);
        rewardIssuance.setReward(createdReward);
        rewardIssuance.setContext("server.test.expires");
        rewardIssuance.setState(ISSUED);
        rewardIssuance.setType(NON_PERSISTENT);
        final long expirationTimestamp = currentTimeMillis() + 3000;
        rewardIssuance.setExpirationTimestamp(expirationTimestamp);

        final RewardIssuance createdRewardIssuance = getRewardIssuanceDao().createRewardIssuance(rewardIssuance);
        assertNotNull(createdRewardIssuance.getId());
        assertEquals((long)createdRewardIssuance.getExpirationTimestamp(), expirationTimestamp);

        try {
            final RewardIssuance fetchedRewardIssuance =
                    getRewardIssuanceDao().getRewardIssuance(createdRewardIssuance.getId());
            assertNotNull(fetchedRewardIssuance);
        }
        catch (NotFoundException e) {
            assertTrue(false, "RewardIssuance should exist at this point.");
        }

        try {
            Thread.sleep(10000);
        }
        catch (InterruptedException e) {
            assertTrue(false, "Thread failed to sleep.");
        }

        try {
            final RewardIssuance fetchedRewardIssuance =
                    getRewardIssuanceDao().getRewardIssuance(createdRewardIssuance.getId());
            assertNull(fetchedRewardIssuance);
        }
        catch (NotFoundException e) {
            // this is expected
        }
    }

    @Test(invocationCount = INVOCATION_COUNT)
    public void testCreateRewardIssuance(ITestContext testContext) {
        int invocation = testContext.getAllTestMethods()[0].getCurrentInvocationCount();

        final Reward reward = new Reward();

        reward.setQuantity(invocation+1);
        reward.setItem(testItem);
        reward.addMetadata("foo", "bar" + invocation);

        final Reward createdReward = getRewardDao().createReward(reward);

        final RewardIssuance rewardIssuance = new RewardIssuance();

        rewardIssuance.setUser(testUser);
        rewardIssuance.setReward(createdReward);
        rewardIssuance.setContext("server.test." + invocation);
        rewardIssuance.setState(ISSUED);
        rewardIssuance.setType(NON_PERSISTENT);

        final RewardIssuance createdRewardIssuance = getRewardIssuanceDao().createRewardIssuance(rewardIssuance);
        assertNotNull(createdRewardIssuance.getId());
        assertEquals(createdRewardIssuance.getUser(), testUser);
        assertEquals(createdRewardIssuance.getReward(), createdReward);
        assertEquals(createdRewardIssuance.getContext(), "server.test."+invocation);
        assertEquals(createdRewardIssuance.getState(), ISSUED);
        assertEquals(createdRewardIssuance.getType(), NON_PERSISTENT);
    }

//    @DataProvider
//    public Object[][] getCreatedRewardIssuances() {
//        final Object[][] objects = getRewardIssuanceDao()
//            .getRewardIssuances(testUser, 0, 20, of(CREATED).collect(toSet()))
//            .getObjects()
//            .stream()
//            .map(pr -> new Object[]{pr})
//            .toArray(Object[][]::new);
//        assertTrue(objects.length > 0);
//        return objects;
//    }
//
//    @DataProvider
//    public Object[][] getRewardIssuances() {
//        final Object[][] objects = getRewardIssuanceDao()
//            .getRewardIssuances(testUser, 0, 20, of(PENDING).collect(toSet()))
//            .getObjects()
//            .stream()
//            .map(pr -> new Object[]{pr})
//            .toArray(Object[][]::new);
//        assertTrue(objects.length > 0);
//        return objects;
//    }
//
//    @Test(dataProvider = "getRewardIssuances")
//    public void testRedeem(final RewardIssuance rewardIssuance) {
//
//        final String id = new MongoInventoryItemId(
//                new ObjectId(testUser.getId()),
//                new ObjectId(testItem.getId()),
//                0).toHexString();
//
//        int existing;
//
//        try {
//            existing = getInventoryItemDao().getInventoryItem(id).getQuantity();
//        } catch (NotFoundException ex) {
//            existing = 0;
//        }
//
//
//        final InventoryItem inventoryItem  = getRewardIssuanceDao().redeem(rewardIssuance);
//        assertEquals(inventoryItem.getUser(), testUser);
//
//        assertEquals(inventoryItem.getId(), id);
//        assertEquals(inventoryItem.getUser(), testUser);
//        assertEquals(inventoryItem.getItem(), testItem);
//        assertEquals(inventoryItem.getPriority(), Integer.valueOf(0));
//        assertEquals(inventoryItem.getQuantity(), Integer.valueOf(existing + rewardIssuance.getReward().getQuantity()));
//
//        final InventoryItem repeatInventoryItem = getRewardIssuanceDao().redeem(rewardIssuance);
//        assertEquals(repeatInventoryItem.getQuantity(), Integer.valueOf(existing + rewardIssuance.getReward().getQuantity()));
//
//        final RewardIssuance postModified = getRewardIssuanceDao().getRewardIssuance(rewardIssuance.getId());
//        assertEquals(postModified.getState(), RewardIssuance.State.REWARDED);
//    }

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

    public RewardDao getRewardDao() {
        return rewardDao;
    }

    @Inject
    public void setRewardDao(RewardDao rewardDao) {
        this.rewardDao = rewardDao;
    }
}
