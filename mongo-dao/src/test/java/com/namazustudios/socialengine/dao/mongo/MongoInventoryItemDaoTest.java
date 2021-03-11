package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.dao.InventoryItemDao;
import com.namazustudios.socialengine.dao.ItemDao;
import com.namazustudios.socialengine.dao.UserDao;
import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.model.goods.Item;
import com.namazustudios.socialengine.model.inventory.InventoryItem;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.namazustudios.socialengine.model.user.User.Level.USER;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.of;
import static org.testng.Assert.*;

@Guice(modules = IntegrationTestModule.class)
public class MongoInventoryItemDaoTest {

    private UserDao userDao;

    private ItemDao itemDao;

    private InventoryItemDao inventoryItemDao;

    private User testUserA;

    private User testUserB;

    private Item testItemA;

    private Item testItemB;

    private Item testItemInsertOnUpdateQuantity;

    private UserTestFactory userTestFactory;

    @BeforeClass
    public void setupTestItems() {

        testItemA = new Item();
        testItemA.setName("item_a");
        testItemA.setDisplayName("Test Item A.");
        testItemA.setDescription("A simple test item.");
        testItemA.setTags(of("a").collect(toList()));
        testItemA.addMetadata("key", "a");

        testItemB = new Item();
        testItemB.setName("item_b");
        testItemB.setDisplayName("Test Item B.");
        testItemB.setDescription("A simple test item.");
        testItemB.setTags(of("a").collect(toList()));
        testItemB.addMetadata("key", "b");

        testItemInsertOnUpdateQuantity = new Item();
        testItemInsertOnUpdateQuantity.setName("item_testItemInsertOnUpdateQuantity");
        testItemInsertOnUpdateQuantity.setDisplayName("Test Item testItemInsertOnUpdateQuantity.");
        testItemInsertOnUpdateQuantity.setDescription("A simple test item.");
        testItemInsertOnUpdateQuantity.setTags(of("testItemInsertOnUpdateQuantity").collect(toList()));
        testItemInsertOnUpdateQuantity.addMetadata("key", "testItemInsertOnUpdateQuantity");

        testItemA = getItemDao().createItem(testItemA);
        testItemB = getItemDao().createItem(testItemB);
        testItemInsertOnUpdateQuantity = getItemDao().createItem(testItemInsertOnUpdateQuantity);

        testUserA = getUserTestFactory().createTestUser();
        testUserB = getUserTestFactory().createTestUser();
    }

    @DataProvider
    public Object[][] getPrioritiesAndQuantities() {
        final Random random = new Random();
        return IntStream
            .range(0, 10)
            .mapToObj(i -> new Object[]{i, random.nextInt(100)})
            .collect(toList())
            .toArray(new Object[][]{});
    }

    @Test(dataProvider = "getPrioritiesAndQuantities")
    public void testCreateInventoryItem(final int priority, final int quantity) {
        testCreateInventoryItem(testUserA, priority, quantity);
        testCreateInventoryItem(testUserB, priority, quantity);
    }

    private void testCreateInventoryItem(final User user, final int priority, final int quantity) {

        InventoryItem inserted = new InventoryItem();
        inserted.setUser(user);
        inserted.setItem(testItemA);
        inserted.setPriority(priority);
        inserted.setQuantity(quantity);
        inserted = getInventoryItemDao().createInventoryItem(inserted);

        InventoryItem fetched = getInventoryItemDao().getInventoryItem(inserted.getId());
        assertEquals(inserted, fetched);
        assertEquals(user, fetched.getUser());
        assertEquals(testItemA, fetched.getItem());
        assertEquals(Integer.valueOf(priority), fetched.getPriority());
        assertEquals(Integer.valueOf(quantity), fetched.getQuantity());

        final Pagination<InventoryItem> items = getInventoryItemDao().getInventoryItems(user, 0, 20);

        assertEquals(items.getTotal(), priority + 1);

        items.forEach(ii -> {
            assertEquals(ii.getUser(), user);
            assertEquals(ii.getItem(), testItemA);
        });

    }

    @DataProvider
    public Object[][] getUsersAndPriorities() {
        return Stream.of(testUserA, testUserB)
            .flatMap(u -> IntStream.range(0, 10).mapToObj(i -> new Object[]{u, i}))
            .toArray(Object[][]::new);
    }

    @Test(dependsOnMethods = "testCreateInventoryItem", dataProvider = "getUsersAndPriorities", expectedExceptions = DuplicateException.class)
    public void testDuplicateInventoryItem(final User user, final int priority) {
        final InventoryItem inserted = new InventoryItem();
        inserted.setUser(user);
        inserted.setItem(testItemA);
        inserted.setPriority(priority);
        inserted.setQuantity(100);
        getInventoryItemDao().createInventoryItem(inserted);
        fail("expected exception by this point.");
    }

    @Test(dependsOnMethods = "testCreateInventoryItem", dataProvider = "getUsersAndPriorities")
    public void testUpdateInventoryItem(final User user, final int priority) {

        final InventoryItem idInventoryItem = getInventoryItemDao().getInventoryItemByItemNameOrId(user, testItemA.getId(), priority);
        final InventoryItem nameInventoryItem = getInventoryItemDao().getInventoryItemByItemNameOrId(user, testItemA.getName(), priority);
        assertEquals(idInventoryItem, nameInventoryItem);
        assertEquals(idInventoryItem.getId(), nameInventoryItem.getId());

        idInventoryItem.setQuantity(0);
        final InventoryItem updatedToZero = getInventoryItemDao().updateInventoryItem(idInventoryItem);

        assertEquals(updatedToZero.getId(), idInventoryItem.getId());
        assertEquals(updatedToZero.getQuantity(), Integer.valueOf(0));

        idInventoryItem.setQuantity(100);
        final InventoryItem updatedToOneHundred = getInventoryItemDao().updateInventoryItem(idInventoryItem);

        assertEquals(updatedToOneHundred.getId(), idInventoryItem.getId());
        assertEquals(updatedToOneHundred.getQuantity(), Integer.valueOf(100));

    }

    @Test(dependsOnMethods = "testUpdateInventoryItem", dataProvider = "getUsersAndPriorities")
    public void testAdjustInventoryItemById(final User user, final int priority) {
        final InventoryItem inventoryItem = getInventoryItemDao().getInventoryItemByItemNameOrId(user, testItemA.getId(), priority);
        final InventoryItem adjustedInventoryItem = getInventoryItemDao().adjustQuantityForItem(user, testItemA.getId(), priority, 50);
        assertEquals(inventoryItem.getId(), adjustedInventoryItem.getId());
        assertEquals(adjustedInventoryItem.getQuantity(), Integer.valueOf(inventoryItem.getQuantity() + 50));
    }

    @Test(dependsOnMethods = "testAdjustInventoryItemById", dataProvider = "getUsersAndPriorities")
    public void testAdjustInventoryItemByName(final User user, final int priority) {
        final InventoryItem inventoryItem = getInventoryItemDao().getInventoryItemByItemNameOrId(user, testItemA.getName(), priority);
        final InventoryItem adjustedInventoryItem = getInventoryItemDao().adjustQuantityForItem(user, testItemA.getName(), priority, 50);
        assertEquals(inventoryItem.getId(), adjustedInventoryItem.getId());
        assertEquals(adjustedInventoryItem.getQuantity(), Integer.valueOf(inventoryItem.getQuantity() + 50));
    }

    @Test(dependsOnMethods = "testAdjustInventoryItemByName", dataProvider = "getUsersAndPriorities")
    public void testSetInventoryItemById(final User user, final int priority) {
        final InventoryItem inventoryItem = getInventoryItemDao().getInventoryItemByItemNameOrId(user, testItemA.getId(), priority);
        final InventoryItem adjustedInventoryItem = getInventoryItemDao().setQuantityForItem(user, testItemA.getId(), priority, 0);
        assertEquals(inventoryItem.getId(), adjustedInventoryItem.getId());
        assertEquals(adjustedInventoryItem.getQuantity(), Integer.valueOf(0));
    }

    @Test(dependsOnMethods = "testSetInventoryItemById", dataProvider = "getUsersAndPriorities")
    public void testCreateInventoryItemByNameAndQuantity(final User user, final int priority) {
        final InventoryItem inventoryItem = getInventoryItemDao().setQuantityForItem(user, testItemInsertOnUpdateQuantity.getName(), priority, 25);
        assertNotNull(inventoryItem.getId());
        assertEquals(inventoryItem.getUser(), user);
        assertEquals(inventoryItem.getItem(), testItemInsertOnUpdateQuantity);
        assertEquals(inventoryItem.getQuantity(), Integer.valueOf(25));
    }

    @Test(dependsOnMethods = "testCreateInventoryItemByNameAndQuantity", dataProvider = "getUsersAndPriorities")
    public void testSetInventoryItemByName(final User user, final int priority) {
        final InventoryItem inventoryItem = getInventoryItemDao().getInventoryItemByItemNameOrId(user, testItemA.getName(), priority);
        final InventoryItem adjustedInventoryItem = getInventoryItemDao().setQuantityForItem(user, testItemA.getName(), priority, 0);
        assertEquals(inventoryItem.getId(), adjustedInventoryItem.getId());
        assertEquals(adjustedInventoryItem.getQuantity(), Integer.valueOf(0));
    }

    @Test(dataProvider = "getUsersAndPriorities", expectedExceptions = NotFoundException.class)
    public void testInventoryItemNotFoundById(final User user, final int priority) {
        getInventoryItemDao().getInventoryItemByItemNameOrId(user, testItemB.getId(), priority);
    }

    @Test(dataProvider = "getUsersAndPriorities", expectedExceptions = NotFoundException.class)
    public void testInventoryItemNotFoundByName(final User user, final int priority) {
        getInventoryItemDao().getInventoryItemByItemNameOrId(user, testItemB.getName(), priority);
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

    public UserTestFactory getUserTestFactory() {
        return userTestFactory;
    }

    @Inject
    public void setUserTestFactory(UserTestFactory userTestFactory) {
        this.userTestFactory = userTestFactory;
    }
}
