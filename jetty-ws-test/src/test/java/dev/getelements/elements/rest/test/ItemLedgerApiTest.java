package dev.getelements.elements.rest.test;

import dev.getelements.elements.sdk.model.goods.CreateItemRequest;
import dev.getelements.elements.sdk.model.goods.Item;
import dev.getelements.elements.sdk.model.inventory.CreateSimpleInventoryItemRequest;
import dev.getelements.elements.sdk.model.inventory.InventoryItem;
import dev.getelements.elements.sdk.model.inventory.ItemLedgerEventType;
import dev.getelements.elements.rest.test.model.ItemLedgerEntryPagination;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;

import static dev.getelements.elements.sdk.model.goods.ItemCategory.FUNGIBLE;
import static dev.getelements.elements.rest.test.TestUtils.TEST_API_ROOT;
import static java.lang.String.format;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.testng.Assert.*;

public class ItemLedgerApiTest {

    @Factory
    public static Object[] getTests() {
        return new Object[]{
            TestUtils.getInstance().getTestFixture(ItemLedgerApiTest.class)
        };
    }

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private Client client;

    @Inject
    private ClientContext user;

    @Inject
    private ClientContext superUser;

    private Item testItem;

    private InventoryItem inventoryItem;

    @Test
    public void createUsers() {
        user.createUser("ledger_test_user").createSession();
        superUser.createSuperuser("ledger_test_admin").createSession();
    }

    @Test(dependsOnMethods = "createUsers")
    public void createDigitalGoods() {
        final var request = new CreateItemRequest();
        request.setCategory(FUNGIBLE);
        request.setName("ledger_test_item");
        request.setDisplayName("Ledger Test Item");
        request.setDescription("Item for ledger testing.");

        final var response = client
            .target(apiRoot + "/item")
            .request()
            .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
            .post(Entity.entity(request, APPLICATION_JSON));

        assertEquals(response.getStatus(), 200);
        testItem = response.readEntity(Item.class);
        assertNotNull(testItem.getId());
    }

    @Test(dependsOnMethods = "createDigitalGoods")
    public void createInventoryItem() {
        final var request = new CreateSimpleInventoryItemRequest();
        request.setItemId(testItem.getId());
        request.setQuantity(10);
        request.setUserId(user.getUser().getId());

        final var response = client
            .target(apiRoot + "/inventory/simple")
            .request()
            .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
            .post(Entity.entity(request, APPLICATION_JSON));

        assertEquals(response.getStatus(), 200);
        inventoryItem = response.readEntity(InventoryItem.class);
        assertNotNull(inventoryItem.getId());
    }

    @Test(dependsOnMethods = "createInventoryItem")
    public void testGetLedgerEntriesByInventoryItemId() {
        final var page = client
            .target(format("%s/inventory/ledger?inventoryItemId=%s", apiRoot, inventoryItem.getId()))
            .request()
            .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
            .get()
            .readEntity(ItemLedgerEntryPagination.class);

        assertTrue(page.getTotal() >= 1, "Expected at least one ledger entry");
        assertFalse(page.getObjects().isEmpty());
        page.getObjects().forEach(e ->
            assertEquals(e.getInventoryItemId(), inventoryItem.getId()));
    }

    @Test(dependsOnMethods = "createInventoryItem")
    public void testGetLedgerEntriesByUserId() {
        final var page = client
            .target(format("%s/inventory/ledger?userId=%s", apiRoot, user.getUser().getId()))
            .request()
            .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
            .get()
            .readEntity(ItemLedgerEntryPagination.class);

        assertTrue(page.getTotal() >= 1, "Expected at least one ledger entry for user");
        page.getObjects().forEach(e ->
            assertEquals(e.getUserId(), user.getUser().getId()));
    }

    @Test(dependsOnMethods = "createInventoryItem")
    public void testGetLedgerEntriesFiltersByEventType() {
        final var page = client
            .target(format("%s/inventory/ledger?inventoryItemId=%s&eventType=%s",
                apiRoot, inventoryItem.getId(), ItemLedgerEventType.CREATED))
            .request()
            .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
            .get()
            .readEntity(ItemLedgerEntryPagination.class);

        assertFalse(page.getObjects().isEmpty(), "Expected at least one CREATED entry");
        page.getObjects().forEach(e ->
            assertEquals(e.getEventType(), ItemLedgerEventType.CREATED));
    }

    @Test(dependsOnMethods = "createInventoryItem")
    public void testGetLedgerEntriesFilteredByUnusedEventTypeReturnsEmpty() {
        final var page = client
            .target(format("%s/inventory/ledger?inventoryItemId=%s&eventType=%s",
                apiRoot, inventoryItem.getId(), ItemLedgerEventType.DELETED))
            .request()
            .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
            .get()
            .readEntity(ItemLedgerEntryPagination.class);

        assertEquals(page.getTotal(), 0);
        assertTrue(page.getObjects().isEmpty());
    }

    @Test(dependsOnMethods = "createUsers")
    public void testGetLedgerEntriesMissingParamsReturnsBadRequest() {
        final var status = client
            .target(apiRoot + "/inventory/ledger")
            .request()
            .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
            .get()
            .getStatus();

        assertEquals(status, 400);
    }

    @Test(dependsOnMethods = "createInventoryItem")
    public void testGetLedgerEntriesAsRegularUserReturnsForbidden() {
        final var status = client
            .target(format("%s/inventory/ledger?inventoryItemId=%s", apiRoot, inventoryItem.getId()))
            .request()
            .header("Authorization", format("Bearer %s", user.getSessionSecret()))
            .get()
            .getStatus();

        assertEquals(status, 403);
    }

    @Test(dependsOnMethods = "createInventoryItem")
    public void testGetLedgerEntriesFiltersByTimestampRange() {
        // Use a future range that contains no entries
        final long far_future = System.currentTimeMillis() + 1_000_000_000L;

        final var emptyPage = client
            .target(format("%s/inventory/ledger?inventoryItemId=%s&from=%d&to=%d",
                apiRoot, inventoryItem.getId(), far_future, far_future + 1000))
            .request()
            .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
            .get()
            .readEntity(ItemLedgerEntryPagination.class);

        assertEquals(emptyPage.getTotal(), 0, "Expected no entries in far-future range");

        // Use a wide range (epoch 0 to far future) — should include the CREATED entry
        final var fullPage = client
            .target(format("%s/inventory/ledger?inventoryItemId=%s&from=0&to=%d",
                apiRoot, inventoryItem.getId(), far_future))
            .request()
            .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
            .get()
            .readEntity(ItemLedgerEntryPagination.class);

        assertTrue(fullPage.getTotal() >= 1, "Expected entries in wide timestamp range");
    }

    @Test(dependsOnMethods = "createInventoryItem")
    public void testPaginationOffsetAndCount() {
        final var firstPage = client
            .target(format("%s/inventory/ledger?inventoryItemId=%s&offset=0&count=1",
                apiRoot, inventoryItem.getId()))
            .request()
            .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
            .get()
            .readEntity(ItemLedgerEntryPagination.class);

        assertEquals(firstPage.getObjects().size(), 1);

        final var emptyPage = client
            .target(format("%s/inventory/ledger?inventoryItemId=%s&offset=1000&count=10",
                apiRoot, inventoryItem.getId()))
            .request()
            .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
            .get()
            .readEntity(ItemLedgerEntryPagination.class);

        assertTrue(emptyPage.getObjects().isEmpty());
    }
}
