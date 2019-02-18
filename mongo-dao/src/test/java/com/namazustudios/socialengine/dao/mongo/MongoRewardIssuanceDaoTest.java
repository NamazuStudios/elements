package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.dao.*;
import com.namazustudios.socialengine.dao.mongo.model.goods.MongoInventoryItemId;
import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.goods.Item;
import com.namazustudios.socialengine.model.inventory.InventoryItem;
import com.namazustudios.socialengine.model.mission.RewardIssuance;
import com.namazustudios.socialengine.model.mission.Reward;
import org.bson.types.ObjectId;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;

import static com.namazustudios.socialengine.model.User.Level.USER;
import static com.namazustudios.socialengine.model.mission.RewardIssuance.State.*;
import static com.namazustudios.socialengine.model.mission.RewardIssuance.Type.*;
import static java.lang.System.currentTimeMillis;
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

    //@Test()
    public void testCreateExpiringRewardIssuance(ITestContext testContext) {
        final RewardIssuance rewardIssuance = new RewardIssuance();

        rewardIssuance.setUser(testUser);
        rewardIssuance.setItem(testItem);
        rewardIssuance.setItemQuantity(5);
        rewardIssuance.setContext("server.test.expires");
        rewardIssuance.setState(ISSUED);
        rewardIssuance.setType(NON_PERSISTENT);
        final long expirationTimestamp = currentTimeMillis() + 3000;
        rewardIssuance.setExpirationTimestamp(expirationTimestamp);

        final RewardIssuance createdRewardIssuance = getRewardIssuanceDao().getOrCreateRewardIssuance(rewardIssuance);
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
            // Mongo's background task to delete expired documents runs every 60 seconds, hence we wait a bit longer.
            // See: https://docs.mongodb.com/manual/core/index-ttl/#timing-of-the-delete-operation
            Thread.sleep(65000);
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
            assertNotNull(e);
            // this is expected
        }
    }

    @Test()
    public void testDuplicateIssuancesForIssuedNonPersistentRewardIssuance() {
        final RewardIssuance rewardIssuance = new RewardIssuance();

        rewardIssuance.setUser(testUser);
        rewardIssuance.setItem(testItem);
        rewardIssuance.setItemQuantity(1);
        rewardIssuance.setContext("server.test.duplicate.issued");
        rewardIssuance.setType(NON_PERSISTENT);
        rewardIssuance.setSource("test");

        final RewardIssuance createdRewardIssuance = getRewardIssuanceDao().getOrCreateRewardIssuance(rewardIssuance);

        final RewardIssuance secondRewardIssuance = new RewardIssuance();

        secondRewardIssuance.setUser(testUser);
        secondRewardIssuance.setItem(testItem);
        secondRewardIssuance.setItemQuantity(1);
        secondRewardIssuance.setContext("server.test.duplicate.issued");
        secondRewardIssuance.setType(NON_PERSISTENT);
        secondRewardIssuance.setSource("test2");

        try {
            final RewardIssuance secondCreatedRewardIssuance =
                    getRewardIssuanceDao().getOrCreateRewardIssuance(secondRewardIssuance);
            assertEquals(secondCreatedRewardIssuance, createdRewardIssuance);   // should not be mutated
        }
        catch (DuplicateException e) {
            assertNotNull(e);
        }
    }

    @Test()
    public void testDuplicateIssuanceForRedeemedPersistentRewardIssuance() {
        final RewardIssuance rewardIssuance = new RewardIssuance();

        rewardIssuance.setUser(testUser);
        rewardIssuance.setItem(testItem);
        rewardIssuance.setItemQuantity(1);
        rewardIssuance.setContext("server.test.duplicate.redeemed");
        rewardIssuance.setType(PERSISTENT);
        rewardIssuance.setSource("test");

        final RewardIssuance createdRewardIssuance = getRewardIssuanceDao().getOrCreateRewardIssuance(rewardIssuance);

        final InventoryItem inventoryItem  = getRewardIssuanceDao().redeem(createdRewardIssuance);

        final RewardIssuance secondRewardIssuance = new RewardIssuance();

        secondRewardIssuance.setUser(testUser);
        secondRewardIssuance.setItem(testItem);
        secondRewardIssuance.setItemQuantity(1);
        secondRewardIssuance.setContext("server.test.duplicate.redeemed");
        secondRewardIssuance.setType(PERSISTENT);
        secondRewardIssuance.setSource("test2");

        try {
            final RewardIssuance secondCreatedRewardIssuance =
                    getRewardIssuanceDao().getOrCreateRewardIssuance(secondRewardIssuance);
            assertEquals(secondCreatedRewardIssuance.getState(), REDEEMED);
        }
        catch (DuplicateException e) {
            assertNotNull(e);
        }
    }

    @Test(invocationCount = INVOCATION_COUNT)
    public void testCreateIssuedPersistentRewardIssuance(ITestContext testContext) {
        int invocation = testContext.getAllTestMethods()[0].getCurrentInvocationCount();

        final RewardIssuance rewardIssuance = new RewardIssuance();

        rewardIssuance.setUser(testUser);
        rewardIssuance.setItem(testItem);
        rewardIssuance.setItemQuantity(1);
        rewardIssuance.setContext("server.test.persistent." + invocation);
        rewardIssuance.setType(PERSISTENT);
        rewardIssuance.setSource("test");

        final RewardIssuance createdRewardIssuance = getRewardIssuanceDao().getOrCreateRewardIssuance(rewardIssuance);
        assertNotNull(createdRewardIssuance.getId());
        assertEquals(createdRewardIssuance.getUser(), testUser);
        assertEquals(createdRewardIssuance.getItem(), testItem);
        assertEquals(createdRewardIssuance.getItemQuantity(), Integer.valueOf(1));
        assertEquals(createdRewardIssuance.getContext(), "server.test.persistent."+invocation);
        assertEquals(createdRewardIssuance.getState(), ISSUED);
        assertEquals(createdRewardIssuance.getType(), PERSISTENT);
        assertEquals(createdRewardIssuance.getSource(), "test");
    }

    @Test(invocationCount = INVOCATION_COUNT)
    public void testCreateIssuedNonPersistentRewardIssuance(ITestContext testContext) {
        int invocation = testContext.getAllTestMethods()[0].getCurrentInvocationCount();

        final RewardIssuance rewardIssuance = new RewardIssuance();

        rewardIssuance.setUser(testUser);
        rewardIssuance.setItem(testItem);
        rewardIssuance.setItemQuantity(1);
        rewardIssuance.setContext("server.test.non-persistent." + invocation);
        rewardIssuance.setType(NON_PERSISTENT);
        rewardIssuance.setSource("test");

        final RewardIssuance createdRewardIssuance = getRewardIssuanceDao().getOrCreateRewardIssuance(rewardIssuance);
        assertNotNull(createdRewardIssuance.getId());
        assertEquals(createdRewardIssuance.getUser(), testUser);
        assertEquals(createdRewardIssuance.getItem(), testItem);
        assertEquals(createdRewardIssuance.getItemQuantity(), Integer.valueOf(1));
        assertEquals(createdRewardIssuance.getContext(), "server.test.non-persistent."+invocation);
        assertEquals(createdRewardIssuance.getState(), ISSUED);
        assertEquals(createdRewardIssuance.getType(), NON_PERSISTENT);
        assertEquals(createdRewardIssuance.getSource(), "test");
    }

    @DataProvider
    public Object[][] getIssuedPersistentRewardIssuances() {
        final Object[][] objects = getRewardIssuanceDao()
            .getRewardIssuances(testUser, 0, 20, of(ISSUED).collect(toSet()))
            .getObjects()
            .stream()
            .filter(ri -> PERSISTENT.equals(ri.getType()))
            .map(ri -> new Object[]{ri})
            .toArray(Object[][]::new);
        assertTrue(objects.length > 0);
        return objects;
    }

    @DataProvider
    public Object[][] getRedeemedPersistentRewardIssuances() {
        final Object[][] objects = getRewardIssuanceDao()
                .getRewardIssuances(testUser, 0, 20, of(REDEEMED).collect(toSet()))
                .getObjects()
                .stream()
                .filter(ri -> PERSISTENT.equals(ri.getType()))
                .map(ri -> new Object[]{ri})
                .toArray(Object[][]::new);
        assertTrue(objects.length > 0);
        return objects;
    }

    @DataProvider
    public Object[][] getIssuedNonPersistentRewardIssuances() {
        final Object[][] objects = getRewardIssuanceDao()
                .getRewardIssuances(testUser, 0, 20, of(ISSUED).collect(toSet()))
                .getObjects()
                .stream()
                .filter(ri -> NON_PERSISTENT.equals(ri.getType()))
                .map(pr -> new Object[]{pr})
                .toArray(Object[][]::new);
        assertTrue(objects.length > 0);
        return objects;
    }


    @Test(dataProvider = "getIssuedPersistentRewardIssuances")
    public void testRedeemPersistent(final RewardIssuance rewardIssuance) {
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
        assertEquals(inventoryItem.getQuantity(), Integer.valueOf(existing + rewardIssuance.getItemQuantity()));

        final RewardIssuance postModified = getRewardIssuanceDao().getRewardIssuance(rewardIssuance.getId());
        assertEquals(postModified.getState(), REDEEMED);
    }

    @Test(dataProvider = "getRedeemedPersistentRewardIssuances", dependsOnMethods = "testRedeemPersistent")
    public void testAlreadyRedeemedPersistent(final RewardIssuance rewardIssuance) {
        try {
            final InventoryItem inventoryItem  = getRewardIssuanceDao().redeem(rewardIssuance);
            assertNull(inventoryItem);
        }
        catch (InvalidDataException e) {
            assertNotNull(e);
            // this is expected
        }
    }

    @Test(dataProvider = "getIssuedNonPersistentRewardIssuances")
    public void testRedeemNonPersistent(final RewardIssuance rewardIssuance) {
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
        assertEquals(inventoryItem.getQuantity(), Integer.valueOf(existing + rewardIssuance.getItemQuantity()));

        try {
            final RewardIssuance postModified = getRewardIssuanceDao().getRewardIssuance(rewardIssuance.getId());
            assertNull(postModified);
        }
        catch (NotFoundException e) {
            assertNotNull(e);
            // this is expected
        }
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
