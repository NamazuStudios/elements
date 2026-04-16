package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.dao.ItemLedgerDao;
import dev.getelements.elements.sdk.model.goods.Item;
import dev.getelements.elements.sdk.model.goods.ItemCategory;
import dev.getelements.elements.sdk.model.inventory.ItemLedgerEntry;
import dev.getelements.elements.sdk.model.inventory.ItemLedgerEventType;
import dev.getelements.elements.sdk.model.user.User;
import jakarta.inject.Inject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.testng.Assert.*;

@Guice(modules = IntegrationTestModule.class)
public class MongoItemLedgerDaoTest {

    private ItemLedgerDao itemLedgerDao;

    private ItemTestFactory itemTestFactory;

    private UserTestFactory userTestFactory;

    private User testUser;

    private Item testItem;

    /** A synthetic inventory-item ID shared across sequential tests. */
    private String testInventoryItemId;

    @BeforeClass
    public void setup() {
        testItem = getItemTestFactory().createTestItem(ItemCategory.FUNGIBLE, "ledgertest", true);
        testUser = getUserTestFactory().createTestUser();
        testInventoryItemId = UUID.randomUUID().toString().replace("-", "");
    }

    @Test
    public void testCreateLedgerEntryReturnsPersistedEntry() {
        final var entry = new ItemLedgerEntry();
        entry.setInventoryItemId(testInventoryItemId);
        entry.setItemCategory(ItemCategory.FUNGIBLE);
        entry.setItemId(testItem.getId());
        entry.setUserId(testUser.getId());
        entry.setActorId(testUser.getId());
        entry.setEventType(ItemLedgerEventType.CREATED);
        entry.setQuantityBefore(0);
        entry.setQuantityAfter(5);

        final var persisted = getItemLedgerDao().createLedgerEntry(entry);

        assertNotNull(persisted.getId());
        assertEquals(persisted.getInventoryItemId(), testInventoryItemId);
        assertEquals(persisted.getItemCategory(), ItemCategory.FUNGIBLE);
        assertEquals(persisted.getItemId(), testItem.getId());
        assertEquals(persisted.getUserId(), testUser.getId());
        assertEquals(persisted.getActorId(), testUser.getId());
        assertEquals(persisted.getEventType(), ItemLedgerEventType.CREATED);
        assertEquals(persisted.getQuantityBefore(), Integer.valueOf(0));
        assertEquals(persisted.getQuantityAfter(), Integer.valueOf(5));
        assertTrue(persisted.getTimestamp() > 0);
    }

    @Test(dependsOnMethods = "testCreateLedgerEntryReturnsPersistedEntry")
    public void testGetLedgerEntriesReturnsEntriesForInventoryItem() {
        // Add a second entry for the same inventory item
        final var entry = new ItemLedgerEntry();
        entry.setInventoryItemId(testInventoryItemId);
        entry.setItemCategory(ItemCategory.FUNGIBLE);
        entry.setItemId(testItem.getId());
        entry.setUserId(testUser.getId());
        entry.setEventType(ItemLedgerEventType.QUANTITY_ADJUSTED);
        entry.setQuantityBefore(5);
        entry.setQuantityAfter(10);
        getItemLedgerDao().createLedgerEntry(entry);

        final var page = getItemLedgerDao().getLedgerEntries(testInventoryItemId, 0, 20, null, null, null);

        assertEquals(page.getTotal(), 2);
        assertEquals(page.getObjects().size(), 2);
        page.getObjects().forEach(e -> assertEquals(e.getInventoryItemId(), testInventoryItemId));

        final var eventTypes = page.getObjects().stream()
                .map(ItemLedgerEntry::getEventType)
                .toList();
        assertTrue(eventTypes.contains(ItemLedgerEventType.CREATED));
        assertTrue(eventTypes.contains(ItemLedgerEventType.QUANTITY_ADJUSTED));
    }

    @Test(dependsOnMethods = "testGetLedgerEntriesReturnsEntriesForInventoryItem")
    public void testGetLedgerEntriesFiltersByEventType() {
        final var page = getItemLedgerDao()
                .getLedgerEntries(testInventoryItemId, 0, 20, ItemLedgerEventType.CREATED, null, null);

        assertEquals(page.getTotal(), 1);
        assertEquals(page.getObjects().get(0).getEventType(), ItemLedgerEventType.CREATED);
    }

    @Test(dependsOnMethods = "testCreateLedgerEntryReturnsPersistedEntry")
    public void testGetLedgerEntriesForUserReturnsAllUserEntries() {
        final var page = getItemLedgerDao()
                .getLedgerEntriesForUser(testUser.getId(), 0, 20, null, null, null);

        assertTrue(page.getTotal() >= 1);
        page.getObjects().forEach(e -> assertEquals(e.getUserId(), testUser.getId()));
    }

    @Test(dependsOnMethods = "testGetLedgerEntriesReturnsEntriesForInventoryItem")
    public void testGetLedgerEntriesForUserFiltersByEventType() {
        final var page = getItemLedgerDao()
                .getLedgerEntriesForUser(testUser.getId(), 0, 20, ItemLedgerEventType.QUANTITY_ADJUSTED, null, null);

        assertTrue(page.getTotal() >= 1);
        page.getObjects().forEach(e -> assertEquals(e.getEventType(), ItemLedgerEventType.QUANTITY_ADJUSTED));
    }

    @Test
    public void testGetLedgerEntriesForUnknownInventoryItemReturnsEmpty() {
        final var page = getItemLedgerDao()
                .getLedgerEntries(UUID.randomUUID().toString(), 0, 20, null, null, null);

        assertEquals(page.getTotal(), 0);
        assertTrue(page.getObjects().isEmpty());
    }

    @Test
    public void testGetLedgerEntriesFiltersByTimestampRange() {
        final var rangeInvId = UUID.randomUUID().toString().replace("-", "");

        // Record "before" time, insert entry, record "after" time
        final long before = System.currentTimeMillis() - 1;

        final var entry = new ItemLedgerEntry();
        entry.setInventoryItemId(rangeInvId);
        entry.setItemCategory(ItemCategory.FUNGIBLE);
        entry.setItemId(testItem.getId());
        entry.setUserId(testUser.getId());
        entry.setEventType(ItemLedgerEventType.CREATED);
        getItemLedgerDao().createLedgerEntry(entry);

        final long after = System.currentTimeMillis() + 1;

        // Query with range that spans the entry — should return 1
        final var inRange = getItemLedgerDao()
                .getLedgerEntries(rangeInvId, 0, 20, null, before, after);
        assertEquals(inRange.getTotal(), 1);

        // Query with upper bound before the entry was created — should return 0
        final var beforeRange = getItemLedgerDao()
                .getLedgerEntries(rangeInvId, 0, 20, null, null, before);
        assertEquals(beforeRange.getTotal(), 0);

        // Query with lower bound after the entry was created — should return 0
        final var afterRange = getItemLedgerDao()
                .getLedgerEntries(rangeInvId, 0, 20, null, after, null);
        assertEquals(afterRange.getTotal(), 0);
    }

    @Test
    public void testPaginationLimitsResults() {
        final var paginationInvId = UUID.randomUUID().toString();

        for (int i = 0; i < 5; i++) {
            final var entry = new ItemLedgerEntry();
            entry.setInventoryItemId(paginationInvId);
            entry.setItemCategory(ItemCategory.FUNGIBLE);
            entry.setItemId(testItem.getId());
            entry.setUserId(testUser.getId());
            entry.setEventType(ItemLedgerEventType.QUANTITY_ADJUSTED);
            getItemLedgerDao().createLedgerEntry(entry);
        }

        final var firstPage = getItemLedgerDao().getLedgerEntries(paginationInvId, 0, 2, null, null, null);
        assertEquals(firstPage.getTotal(), 5);
        assertEquals(firstPage.getObjects().size(), 2);

        final var secondPage = getItemLedgerDao().getLedgerEntries(paginationInvId, 2, 2, null, null, null);
        assertEquals(secondPage.getTotal(), 5);
        assertEquals(secondPage.getObjects().size(), 2);

        final var lastPage = getItemLedgerDao().getLedgerEntries(paginationInvId, 4, 2, null, null, null);
        assertEquals(lastPage.getTotal(), 5);
        assertEquals(lastPage.getObjects().size(), 1);
    }

    public ItemLedgerDao getItemLedgerDao() {
        return itemLedgerDao;
    }

    @Inject
    public void setItemLedgerDao(final ItemLedgerDao itemLedgerDao) {
        this.itemLedgerDao = itemLedgerDao;
    }

    public ItemTestFactory getItemTestFactory() {
        return itemTestFactory;
    }

    @Inject
    public void setItemTestFactory(final ItemTestFactory itemTestFactory) {
        this.itemTestFactory = itemTestFactory;
    }

    public UserTestFactory getUserTestFactory() {
        return userTestFactory;
    }

    @Inject
    public void setUserTestFactory(final UserTestFactory userTestFactory) {
        this.userTestFactory = userTestFactory;
    }
}
