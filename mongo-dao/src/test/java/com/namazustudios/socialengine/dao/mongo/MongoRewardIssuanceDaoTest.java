package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.dao.*;
import com.namazustudios.socialengine.dao.mongo.model.goods.MongoInventoryItemId;
import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.model.goods.Item;
import com.namazustudios.socialengine.model.inventory.InventoryItem;
import com.namazustudios.socialengine.model.reward.RewardIssuance;
import org.bson.types.ObjectId;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.namazustudios.socialengine.model.user.User.Level.USER;
import static com.namazustudios.socialengine.model.reward.RewardIssuance.State;
import static com.namazustudios.socialengine.model.reward.RewardIssuance.State.*;
import static com.namazustudios.socialengine.model.reward.RewardIssuance.Type.*;
import static java.lang.System.currentTimeMillis;
import static java.util.stream.Collectors.*;
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
        testItem.setTags(of("a").collect(toList()));
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
        rewardIssuance.addTag("tagtest");

        final RewardIssuance createdRewardIssuance = getRewardIssuanceDao().getOrCreateRewardIssuance(rewardIssuance);
        assertNotNull(createdRewardIssuance.getId());
        assertEquals(createdRewardIssuance.getUser(), testUser);
        assertEquals(createdRewardIssuance.getItem(), testItem);
        assertEquals(createdRewardIssuance.getItemQuantity(), Integer.valueOf(1));
        assertEquals(createdRewardIssuance.getContext(), "server.test.persistent."+invocation);
        assertEquals(createdRewardIssuance.getState(), ISSUED);
        assertEquals(createdRewardIssuance.getType(), PERSISTENT);
        assertEquals(createdRewardIssuance.getSource(), "test");
        assertTrue(createdRewardIssuance.getTags().contains("tagtest"));
        assertEquals(createdRewardIssuance.getTags().size(), 1);
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
            .getRewardIssuances(testUser, 0, 20, of(ISSUED).collect(toList()), null)
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
                .getRewardIssuances(testUser, 0, 20, of(REDEEMED).collect(toList()), null)
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
                .getRewardIssuances(testUser, 0, 20, of(ISSUED).collect(toList()), null)
                .getObjects()
                .stream()
                .filter(ri -> NON_PERSISTENT.equals(ri.getType()))
                .map(pr -> new Object[]{pr})
                .toArray(Object[][]::new);
        assertTrue(objects.length > 0);
        return objects;
    }

    @DataProvider
    public Object[][] getAllRewardIssuances() {
        final Object[][] objects = getRewardIssuanceDao()
                .getRewardIssuances(testUser, 0, 50, null, null)
                .getObjects()
                .stream()
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

    @Test(dataProvider = "getAllRewardIssuances",
            dependsOnMethods = {"testRedeemPersistent", "testAlreadyRedeemedPersistent", "testRedeemNonPersistent"})
    public void deleteIssuances(final RewardIssuance rewardIssuance) {
        getRewardIssuanceDao().delete(rewardIssuance.getId());
    }

    @Test(dependsOnMethods = {"deleteIssuances"})
    public void testGetByStatesAndTags() {
        final int creationCount = 12;
        final int issuedCount = 10; // indices 0-9 will be issued, 10 and 11 will be redeemed
        final int redeemedCount = 2;
        final int evenCount = 6;
        final int oddCount = 6;
        final int issuedEvenCount = 5;
        final int issuedOddCount = 5;
        final int redeemedEvenCount = 1;
        final int redeemedOddCount = 1;

        for (int i=0; i < creationCount; i++) {
            final RewardIssuance rewardIssuance = new RewardIssuance();

            rewardIssuance.setUser(testUser);
            rewardIssuance.setItem(testItem);
            rewardIssuance.setItemQuantity(5);
            rewardIssuance.setContext("server.test.states_and_tags."+i);

            rewardIssuance.setState(ISSUED);

            rewardIssuance.setType(PERSISTENT);

            List<String> tags = new ArrayList<>();
            if (i %2 == 0) {
                tags.add("even");
            }
            else {
                tags.add("odd");
            }

            tags.add("tag"+i);

            rewardIssuance.setTags(tags);

            final RewardIssuance createdRewardIssuance =
                    getRewardIssuanceDao().getOrCreateRewardIssuance(rewardIssuance);

            assertNotNull(createdRewardIssuance);
            assertEquals(createdRewardIssuance.getTags(), rewardIssuance.getTags());

            if (i %2 == 0) {
                assertTrue(createdRewardIssuance.getTags().contains("even"));
                assertFalse(createdRewardIssuance.getTags().contains("odd"));
            }
            else {
                assertTrue(createdRewardIssuance.getTags().contains("odd"));
                assertFalse(createdRewardIssuance.getTags().contains("even"));
            }
            assertTrue(createdRewardIssuance.getTags().contains("tag"+i));

            // we need to manually redeem the last two issuances to set their state as REDEEMED (creation always forces
            // ISSUED state before insertion)
            if (i >= issuedCount) {
                getRewardIssuanceDao().redeem(createdRewardIssuance);
            }
        }

        final List<State> combinedStates = new ArrayList<>();
        combinedStates.add(ISSUED);
        combinedStates.add(REDEEMED);

        // States-only tests
        final List<RewardIssuance> combinedStatesIssuances =
                getRewardIssuanceDao().getRewardIssuances(testUser,0, 50, combinedStates, null)
                .getObjects();
        assertEquals(combinedStatesIssuances.size(), creationCount);
        final Set<State> combinedStatesRetrieved = combinedStatesIssuances.stream().map(ri -> ri.getState()).collect(Collectors.toSet());
        assertTrue(combinedStatesRetrieved.containsAll(combinedStates));

        final List<RewardIssuance> issuedStatesIssuances =
                getRewardIssuanceDao().getRewardIssuances(testUser,0, 50, of(ISSUED).collect(toList()), null)
                        .getObjects();
        assertEquals(issuedStatesIssuances.size(), issuedCount);
        final Set<State> issuedStates = issuedStatesIssuances.stream().map(ri -> ri.getState()).collect(Collectors.toSet());
        assertTrue(issuedStates.contains(ISSUED));
        assertEquals(issuedStates.size(), 1);

        final List<RewardIssuance> redeemedStatesIssuances =
                getRewardIssuanceDao().getRewardIssuances(testUser,0, 50, of(REDEEMED).collect(toList()), null)
                        .getObjects();
        assertEquals(redeemedStatesIssuances.size(), redeemedCount);
        final Set<State> redeemedStates = redeemedStatesIssuances.stream().map(ri -> ri.getState()).collect(Collectors.toSet());
        assertTrue(redeemedStates.contains(REDEEMED));
        assertEquals(redeemedStates.size(), 1);


        // Tags-only tests
        for (int i=0; i < creationCount; i++) {
            final List<RewardIssuance> currentCountTagIssuances =
                    getRewardIssuanceDao().getRewardIssuances(testUser,0, 50, null, of("tag"+i).collect(toList()))
                            .getObjects();
            assertEquals(currentCountTagIssuances.size(), 1);
        }

        final List<String> combinedEvenOddTags = new ArrayList<>();
        combinedEvenOddTags.add("even");
        combinedEvenOddTags.add("odd");
        final List<RewardIssuance> combinedEvenOddTagIssuances =
                getRewardIssuanceDao().getRewardIssuances(testUser,0, 50, null, combinedEvenOddTags)
                        .getObjects();
        assertTrue(combinedEvenOddTagIssuances.size() == (evenCount + oddCount));
        final long combinedEvenCount = combinedEvenOddTagIssuances.stream().filter(ri -> ri.getTags().contains("even")).count();
        assertEquals(combinedEvenCount, evenCount);
        final long combinedOddCount = combinedEvenOddTagIssuances.stream().filter(ri -> ri.getTags().contains("odd")).count();
        assertEquals(combinedOddCount, oddCount);
        for (int i=0; i<evenCount+oddCount; i++) {
            final int lambdaI = i;
            final long currentOrdinalCount = combinedEvenOddTagIssuances.stream().filter(ri -> ri.getTags().contains("tag"+lambdaI)).count();
            assertEquals(currentOrdinalCount, 1);
        }

        final List<RewardIssuance> evenTagIssuances =
                getRewardIssuanceDao().getRewardIssuances(testUser,0, 50, null, of("even").collect(toList()))
                        .getObjects();
        assertEquals(evenTagIssuances.size(), evenCount);

        final Set<String> evenTagContexts = evenTagIssuances.stream().map(ri -> ri.getContext()).collect(Collectors.toSet());
        for (int i=0; i<creationCount; i++) {
            if (i %2 == 0) {
                assertTrue(evenTagContexts.contains("server.test.states_and_tags."+i));
            }
        }

        final List<RewardIssuance> oddTagIssuances =
                getRewardIssuanceDao().getRewardIssuances(testUser,0, 50, null, of("odd").collect(toList()))
                        .getObjects();
        assertTrue(oddTagIssuances.size() == evenCount);
        final Set<String> oddTagContexts = oddTagIssuances.stream().map(ri -> ri.getContext()).collect(Collectors.toSet());
        for (int i=0; i<creationCount; i++) {
            if (i %2 != 0) {
                assertTrue(oddTagContexts.contains("server.test.states_and_tags."+i));
            }
        }

        final List<RewardIssuance> nonexistentTagIssuances =
                getRewardIssuanceDao().getRewardIssuances(testUser,0, 50, null, of("none").collect(toList()))
                        .getObjects();
        assertEquals(nonexistentTagIssuances.size(), 0);


        // States+tags tests
        final List<RewardIssuance> issuedEvenIssuances =
                getRewardIssuanceDao().getRewardIssuances(testUser,0, 50, of(ISSUED).collect(toList()), of("even").collect(toList()))
                        .getObjects();
        assertEquals(issuedEvenIssuances.size(), issuedEvenCount);

        final List<RewardIssuance> issuedOddIssuances =
                getRewardIssuanceDao().getRewardIssuances(testUser,0, 50, of(ISSUED).collect(toList()), of("odd").collect(toList()))
                        .getObjects();
        assertEquals(issuedOddIssuances.size(), issuedEvenCount);

        final List<RewardIssuance> redeemedEvenIssuances =
                getRewardIssuanceDao().getRewardIssuances(testUser,0, 50, of(REDEEMED).collect(toList()), of("even").collect(toList()))
                        .getObjects();
        assertEquals(redeemedEvenIssuances.size(), redeemedEvenCount);

        final List<RewardIssuance> redeemedOddIssuances =
                getRewardIssuanceDao().getRewardIssuances(testUser,0, 50, of(REDEEMED).collect(toList()), of("odd").collect(toList()))
                        .getObjects();
        assertEquals(redeemedOddIssuances.size(), redeemedEvenCount);
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
