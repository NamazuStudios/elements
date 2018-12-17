package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.dao.InventoryItemDao;
import com.namazustudios.socialengine.dao.ItemDao;
import com.namazustudios.socialengine.dao.UserDao;
import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
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

import static com.namazustudios.socialengine.model.User.Level.USER;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.of;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

@Guice(modules = IntegrationTestModule.class)
public class MongoInventoryItemDaoTest {

    private UserDao userDao;

    private ItemDao itemDao;

    private InventoryItemDao inventoryItemDao;

    private User testUserA;

    private User testUserB;

    private Item testItemA;

    private Item testItemB;

    @BeforeClass
    public void setupTetItems() {

        testUserA = new User();
        testUserA.setName("testy.mctesterson.2");
        testUserA.setEmail("testy.mctesterson.2@example.com");
        testUserA.setLevel(USER);

        testUserB = new User();
        testUserB.setName("testy.mctesterson.3");
        testUserB.setEmail("testy.mctesterson.3@example.com");
        testUserB.setLevel(USER);

        testItemA = new Item();
        testItemA.setName("item_a");
        testItemA.setDisplayName("Test Item A.");
        testItemA.setDescription("A simple test item.");
        testItemA.setTags(of("a").collect(toSet()));
        testItemA.addMetadata("key", "a");

        testItemB = new Item();
        testItemB.setName("item_b");
        testItemB.setDisplayName("Test Item B.");
        testItemB.setDescription("A simple test item.");
        testItemB.setTags(of("a").collect(toSet()));
        testItemB.addMetadata("key", "b");

        testItemA = getItemDao().createItem(testItemA);
        testItemB = getItemDao().createItem(testItemB);
        testUserA = getUserDao().createOrReactivateUser(testUserA);
        testUserB = getUserDao().createOrReactivateUser(testUserB);
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
        inserted.setPriority(0);
        inserted.setQuantity(100);
        getInventoryItemDao().createInventoryItem(inserted);
        fail("expected exception by this point.");
    }

    @Test(dependsOnMethods = "testCreateInventoryItem", dataProvider = "getUsersAndPriorities")
    public void testUpdateInventoryItem(final User user, final int priority) {

        final InventoryItem idInventoryItem = getInventoryItemDao().getInventoryItemByItemNameOrId(user, testItemA.getId(), priority);
        final InventoryItem nameInventoryItem = getInventoryItemDao().getInventoryItemByItemNameOrId(user, testItemA.getName(), priority);
        assertEquals(idInventoryItem, nameInventoryItem);

        idInventoryItem.setQuantity(0);
        final InventoryItem updatedToZero = getInventoryItemDao().updateInventoryItem(idInventoryItem);
        assertEquals(updatedToZero.getQuantity(), Integer.valueOf(0));

        idInventoryItem.setQuantity(100);
        final InventoryItem updatedToOneHundred = getInventoryItemDao().updateInventoryItem(idInventoryItem);
        assertEquals(updatedToOneHundred.getQuantity(), Integer.valueOf(100));

    }

    @Test(dependsOnMethods = "testUpdateInventoryItem", dataProvider = "getUsersAndPriorities")
    public void testUAdjustInventoryItemById(final User user, final int priority) {
        final InventoryItem inventoryItem = getInventoryItemDao().getInventoryItemByItemNameOrId(user, testItemA.getId(), priority);
        final InventoryItem adjustedInventoryItem = getInventoryItemDao().adjustQuantityForItem(user, testItemA.getId(), priority, 50);
        assertEquals(inventoryItem.getId(), adjustedInventoryItem.getId());
        assertEquals(adjustedInventoryItem.getQuantity(), Integer.valueOf(inventoryItem.getQuantity() + 50));
    }

    @Test(dependsOnMethods = "testUAdjustInventoryItemById", dataProvider = "getUsersAndPriorities")
    public void testUAdjustInventoryItemByName(final User user, final int priority) {
        final InventoryItem inventoryItem = getInventoryItemDao().getInventoryItemByItemNameOrId(user, testItemA.getName(), priority);
        final InventoryItem adjustedInventoryItem = getInventoryItemDao().adjustQuantityForItem(user, testItemA.getName(), priority, 50);
        assertEquals(inventoryItem.getId(), adjustedInventoryItem.getId());
        assertEquals(adjustedInventoryItem.getQuantity(), Integer.valueOf(inventoryItem.getQuantity() + 50));
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

}
