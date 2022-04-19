package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.goods.CreateItemRequest;
import com.namazustudios.socialengine.model.goods.Item;
import com.namazustudios.socialengine.model.goods.ItemCategory;
import com.namazustudios.socialengine.model.inventory.*;
import com.namazustudios.socialengine.rest.model.InventoryItemPagination;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import static com.namazustudios.socialengine.model.goods.ItemCategory.FUNGIBLE;
import static com.namazustudios.socialengine.rest.TestUtils.TEST_API_ROOT;
import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.testng.Assert.*;
import static org.testng.Assert.assertEquals;

public class AdvancedInventoryApiTest {

    @Factory
    public static Object[] getTests() {
        return new Object[] {
            TestUtils.getInstance().getXodusTest(AdvancedInventoryApiTest.class),
            TestUtils.getInstance().getUnixFSTest(AdvancedInventoryApiTest.class)
        };
    }

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private Client client;

    @Inject
    private ClientContext user0;

    @Inject
    private ClientContext user1;

    @Inject
    private ClientContext superUser;

    private Item itemA;

    private Item itemB;

    @Test
    public void createUsers() {
        user0.createUser("advanced_inventory_user0").createSession();
        user1.createUser("advanced_inventory_user1").createSession();
        superUser.createSuperuser("advanced_inventory_admin").createSession();
    }

    @Test(dependsOnMethods = "createUsers")
    public void createDigitalGoods() {

        final var aRequest = new CreateItemRequest();
        aRequest.setCategory(FUNGIBLE);
        aRequest.setName("advanced_inventory_test_item_a");
        aRequest.setDisplayName("Test Item A");
        aRequest.setDescription("Test Item A (More)");
        aRequest.setTags(List.of("item_a", "item"));

        final var aMetadata = new HashMap<String, Object>();
        aMetadata.put("test", "item_a");
        aRequest.setMetadata(aMetadata);

        final var bRequest = new CreateItemRequest();
        bRequest.setCategory(FUNGIBLE);
        bRequest.setName("advanced_inventory_test_item_b");
        bRequest.setDisplayName("Test Item B");
        bRequest.setDescription("Test Item B (More)");
        bRequest.setTags(List.of("item_b", "item"));

        final var bMetadata = new HashMap<String, Object>();
        bMetadata.put("test", "item_b");
        bRequest.setMetadata(bMetadata);

        itemA = createItem(aRequest);
        itemB = createItem(bRequest);

    }

    private Item createItem(final CreateItemRequest createItemRequest) {

        final var response = client
                .target(apiRoot + "/item")
                .request()
                .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
                .post(Entity.entity(createItemRequest, APPLICATION_JSON));

        assertEquals(response.getStatus(), 200);

        return response.readEntity(Item.class);

    }

    @DataProvider
    public Object[][] getUserContext() {
        return new Object[][] {
                new Object[] {user0, 0},
                new Object[] {user0, 1},
                new Object[] {user1, 0},
                new Object[] {user1, 1},
        };
    }

    @Test(dependsOnMethods = {"createUsers", "createDigitalGoods"}, dataProvider = "getUserContext")
    public void testRegularUserIsDenied(final ClientContext userContext, final int priority) {

        final var create = new CreateAdvancedInventoryItemRequest();
        create.setItemId(itemA.getId());
        create.setQuantity(10);
        create.setPriority(priority);
        create.setUserId(userContext.getUser().getId());

        var response = client
                .target(apiRoot + "/inventory/advanced")
                .request()
                .header("Authorization", format("Bearer %s", userContext.getSessionSecret()))
                .post(Entity.entity(create, APPLICATION_JSON));

        assertEquals(response.getStatus(), 403);

        final var update = new UpdateInventoryItemRequest();
        update.setQuantity(10);

        response = client
                .target(apiRoot + "/inventory/advanced/bogo")
                .request()
                .header("Authorization", format("Bearer %s", userContext.getSessionSecret()))
                .put(Entity.entity(update, APPLICATION_JSON));

        assertEquals(response.getStatus(), 403);

        final var adjustment = new AdvancedInventoryItemQuantityAdjustment();

        adjustment.setQuantityDelta(5);
        adjustment.setPriority(priority);
        adjustment.setUserId(userContext.getUser().getId());

        response = client
                .target(apiRoot + "/inventory/advanced/bogo")
                .request()
                .header("X-HTTP-Method-Override", "PATCH")
                .header("Authorization", format("Bearer %s", userContext.getSessionSecret()))
                .post(Entity.entity(adjustment, APPLICATION_JSON));

        assertEquals(response.getStatus(), 403);

    }

    @Test(dependsOnMethods = {"createUsers", "createDigitalGoods"}, dataProvider = "getUserContext")
    public void checkEmptyInventory(final ClientContext userContext, final int priority) {

        final var response = client
                .target(apiRoot + "/inventory/advanced")
                .request()
                .header("Authorization", format("Bearer %s", userContext.getSessionSecret()))
                .get()
                .readEntity(Pagination.class);

        assertTrue(response.getObjects().isEmpty(), "Expected empty object response.");

    }

    @Test(dependsOnMethods = {"checkEmptyInventory", "createUsers"}, dataProvider = "getUserContext")
    public void testCreateInventoryItems(final ClientContext userContext, final int priority) {

        final var request = new CreateAdvancedInventoryItemRequest();
        request.setPriority(priority);
        request.setItemId(itemA.getId());
        request.setQuantity(10);
        request.setUserId(userContext.getUser().getId());

        final var response = client
            .target(apiRoot + "/inventory/advanced")
            .request()
            .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
            .post(Entity.entity(request, APPLICATION_JSON))
            .readEntity(InventoryItem.class);

        assertNotNull(response.getId());
        assertEquals(response.getItem().getId(), itemA.getId());
        assertEquals(response.getUser().getId(), userContext.getUser().getId());
        assertEquals(response.getQuantity(), 10);
        assertEquals(response.getPriority(), priority);

    }

    @Test(dependsOnMethods = {"testCreateInventoryItems"}, dataProvider = "getUserContext")
    public void testAdjustInventoryItems(final ClientContext userContext, final int priority) {

        final var items = client
            .target(apiRoot + "/inventory/advanced")
            .request()
            .header("Authorization", format("Bearer %s", userContext.getSessionSecret()))
            .get()
            .readEntity(InventoryItemPagination.class);

        assertFalse(items.getObjects().isEmpty(), "Expected non-empty object response.");

        final var inventoryItem = items.getObjects()
            .stream()
            .filter(i -> i.getItem().getName().equals("advanced_inventory_test_item_a") && i.getPriority() == priority)
            .findFirst()
            .get();

        final var request = new AdvancedInventoryItemQuantityAdjustment();

        request.setQuantityDelta(5);
        request.setPriority(priority);
        request.setUserId(userContext.getUser().getId());

        final var response = client
            .target(format("%s/inventory/advanced/%s", apiRoot, inventoryItem.getId()))
            .request()
            .header("X-HTTP-Method-Override", "PATCH")
            .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
            .post(Entity.entity(request, APPLICATION_JSON))
            .readEntity(InventoryItem.class);

        assertNotNull(response.getId());
        assertEquals(response.getItem().getId(), itemA.getId());
        assertEquals(response.getUser().getId(), userContext.getUser().getId());
        assertEquals(response.getQuantity(), 15);
        assertEquals(response.getPriority(), priority);

    }

    @Test(dependsOnMethods = {"testAdjustInventoryItems"}, dataProvider = "getUserContext")
    public void testUpdateInventoryItems(final ClientContext userContext, final int priority) {

        final var items = client
            .target(apiRoot + "/inventory/advanced")
            .request()
            .header("Authorization", format("Bearer %s", userContext.getSessionSecret()))
            .get()
            .readEntity(InventoryItemPagination.class);

        assertFalse(items.getObjects().isEmpty(), "Expected non-empty object response.");

        final var inventoryItem = items.getObjects()
            .stream()
            .filter(i -> i.getItem().getName().equals("advanced_inventory_test_item_a") && i.getPriority() == priority)
            .findFirst()
            .get();

        final var request = new UpdateInventoryItemRequest();
        request.setQuantity(20);

        final var response = client
            .target(format("%s/inventory/advanced/%s", apiRoot, inventoryItem.getId()))
            .request()
            .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
            .put(Entity.entity(request, APPLICATION_JSON))
            .readEntity(InventoryItem.class);

        assertNotNull(response.getId());
        assertEquals(response.getItem().getId(), itemA.getId());
        assertEquals(response.getUser().getId(), userContext.getUser().getId());
        assertEquals(response.getQuantity(), 20);
        assertEquals(response.getPriority(), priority);

    }

    @Test(dependsOnMethods = {"testUpdateInventoryItems"}, dataProvider = "getUserContext")
    public void testCheckNonEmptyInventory(final ClientContext userContext, final int priority) {

        final var items = client
            .target(apiRoot + "/inventory/advanced")
            .request()
            .header("Authorization", format("Bearer %s", userContext.getSessionSecret()))
            .get()
            .readEntity(InventoryItemPagination.class);

        assertEquals(items.getObjects().size(), 2);

        final var item = items.getObjects()
            .stream()
            .filter(i -> i.getPriority() == priority)
            .findFirst()
            .get();

        assertEquals(item.getPriority(), priority);
        assertEquals(item.getQuantity(), 20);
        assertEquals(item.getUser().getId(), userContext.getUser().getId());
        assertEquals(item.getItem().getId(), itemA.getId());

    }

    @Test(dependsOnMethods = "testCheckNonEmptyInventory", dataProvider = "getUserContext")
    public void testDeleteInventoryItem(final ClientContext userContext, final int priority) {

        final var items = client
            .target(apiRoot + "/inventory/advanced")
            .request()
            .header("Authorization", format("Bearer %s", userContext.getSessionSecret()))
            .get()
            .readEntity(InventoryItemPagination.class);

        assertFalse(items.getObjects().isEmpty(), "Expected non-empty object response.");

        final var inventoryItem = items.getObjects()
            .stream()
            .filter(i -> i.getItem().getName().equals("advanced_inventory_test_item_a") && i.getPriority() == priority)
            .findFirst()
            .get();

        final var response = client
            .target(format("%s/inventory/advanced/%s", apiRoot, inventoryItem.getId()))
            .request()
            .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
            .delete();

        assertEquals(response.getStatus(), 204);

    }

}
