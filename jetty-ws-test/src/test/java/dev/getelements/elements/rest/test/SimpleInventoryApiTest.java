package dev.getelements.elements.rest.test;

import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.goods.CreateItemRequest;
import dev.getelements.elements.model.goods.Item;
import dev.getelements.elements.model.inventory.CreateSimpleInventoryItemRequest;
import dev.getelements.elements.model.inventory.InventoryItem;
import dev.getelements.elements.model.inventory.SimpleInventoryItemQuantityAdjustment;
import dev.getelements.elements.model.inventory.UpdateInventoryItemRequest;
import dev.getelements.elements.rest.test.ClientContext;
import dev.getelements.elements.rest.test.TestUtils;
import dev.getelements.elements.rest.test.model.InventoryItemPagination;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import java.util.HashMap;
import java.util.List;

import static dev.getelements.elements.model.goods.ItemCategory.FUNGIBLE;
import static dev.getelements.elements.rest.test.TestUtils.TEST_API_ROOT;
import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.testng.Assert.*;

public class SimpleInventoryApiTest {

    @Factory
    public static Object[] getTests() {
        return new Object[] {
            TestUtils.getInstance().getXodusTest(SimpleInventoryApiTest.class),
            TestUtils.getInstance().getUnixFSTest(SimpleInventoryApiTest.class)
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
        user0.createUser("simple_inventory_user0").createSession();
        user1.createUser("simple_inventory_user1").createSession();
        superUser.createSuperuser("simple_inventory_admin").createSession();
    }

    @Test(dependsOnMethods = "createUsers")
    public void createDigitalGoods() {

        final var aRequest = new CreateItemRequest();
        aRequest.setCategory(FUNGIBLE);
        aRequest.setName("simple_inventory_test_item_a");
        aRequest.setDisplayName("Test Item A");
        aRequest.setDescription("Test Item A (More)");
        aRequest.setTags(List.of("item_a", "item"));

        final var aMetadata = new HashMap<String, Object>();
        aMetadata.put("test", "item_a");
        aRequest.setMetadata(aMetadata);

        final var bRequest = new CreateItemRequest();
        bRequest.setCategory(FUNGIBLE);
        bRequest.setName("simple_inventory_test_item_b");
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
            new Object[] {user0},
            new Object[] {user1},
        };
    }

    @Test(dependsOnMethods = {"createUsers", "createDigitalGoods"}, dataProvider = "getUserContext")
    public void testRegularUserIsDenied(final ClientContext userContext) {

        final var create = new CreateSimpleInventoryItemRequest();
        create.setItemId(itemA.getId());
        create.setQuantity(10);
        create.setUserId(userContext.getUser().getId());

        var response = client
            .target(apiRoot + "/inventory/simple")
            .request()
            .header("Authorization", format("Bearer %s", userContext.getSessionSecret()))
            .post(Entity.entity(create, APPLICATION_JSON));

        assertEquals(response.getStatus(), 403);

        final var update = new UpdateInventoryItemRequest();
        update.setQuantity(10);

        response = client
            .target(apiRoot + "/inventory/simple/bogo")
            .request()
            .header("Authorization", format("Bearer %s", userContext.getSessionSecret()))
            .put(Entity.entity(update, APPLICATION_JSON));

        assertEquals(response.getStatus(), 403);

        final var adjustment = new SimpleInventoryItemQuantityAdjustment();

        adjustment.setQuantityDelta(5);
        adjustment.setUserId(userContext.getUser().getId());

        response = client
            .target(apiRoot + "/inventory/simple/bogo")
            .request()
            .header("X-HTTP-Method-Override", "PATCH")
            .header("Authorization", format("Bearer %s", userContext.getSessionSecret()))
            .post(Entity.entity(adjustment, APPLICATION_JSON));

        assertEquals(response.getStatus(), 403);

    }

    @Test(dependsOnMethods = {"createUsers", "createDigitalGoods"}, dataProvider = "getUserContext")
    public void checkEmptyInventory(final ClientContext userContext) {

        final var response = client
            .target(apiRoot + "/inventory/simple")
            .request()
            .header("Authorization", format("Bearer %s", userContext.getSessionSecret()))
            .get()
            .readEntity(Pagination.class);

        assertTrue(response.getObjects().isEmpty(), "Expected empty object response.");

    }

    @Test(dependsOnMethods = {"checkEmptyInventory", "createUsers"}, dataProvider = "getUserContext")
    public void testCreateInventoryItems(final ClientContext userContext) {

        final var request = new CreateSimpleInventoryItemRequest();
        request.setItemId(itemA.getId());
        request.setQuantity(10);
        request.setUserId(userContext.getUser().getId());

        final var response = client
            .target(apiRoot + "/inventory/simple")
            .request()
            .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
            .post(Entity.entity(request, APPLICATION_JSON))
            .readEntity(InventoryItem.class);

        assertNotNull(response.getId());
        assertEquals(response.getItem().getId(), itemA.getId());
        assertEquals(response.getUser().getId(), userContext.getUser().getId());
        assertEquals(response.getQuantity(), 10);
        assertEquals(response.getPriority(), 0);

    }

    @Test(dependsOnMethods = {"testCreateInventoryItems"}, dataProvider = "getUserContext")
    public void testAdjustInventoryItems(final ClientContext userContext) {

        final var items = client
            .target(apiRoot + "/inventory/simple")
            .request()
            .header("Authorization", format("Bearer %s", userContext.getSessionSecret()))
            .get()
            .readEntity(InventoryItemPagination.class);

        assertFalse(items.getObjects().isEmpty(), "Expected non-empty object response.");

        final var inventoryItem = items.getObjects()
            .stream()
            .filter(i -> i.getItem().getName().equals("simple_inventory_test_item_a"))
            .findFirst()
            .get();

        final var request = new SimpleInventoryItemQuantityAdjustment();

        request.setQuantityDelta(5);
        request.setUserId(userContext.getUser().getId());

         final var response = client
            .target(format("%s/inventory/simple/%s", apiRoot, inventoryItem.getId()))
            .request()
            .header("X-HTTP-Method-Override", "PATCH")
            .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
            .post(Entity.entity(request, APPLICATION_JSON))
            .readEntity(InventoryItem.class);

        assertNotNull(response.getId());
        assertEquals(response.getItem().getId(), itemA.getId());
        assertEquals(response.getUser().getId(), userContext.getUser().getId());
        assertEquals(response.getQuantity(), 15);
        assertEquals(response.getPriority(), 0);

    }

    @Test(dependsOnMethods = {"testAdjustInventoryItems"}, dataProvider = "getUserContext")
    public void testUpdateInventoryItems(final ClientContext userContext) {

        final var items = client
            .target(apiRoot + "/inventory/simple")
            .request()
            .header("Authorization", format("Bearer %s", userContext.getSessionSecret()))
            .get()
            .readEntity(InventoryItemPagination.class);

        assertFalse(items.getObjects().isEmpty(), "Expected non-empty object response.");

        final var inventoryItem = items.getObjects()
            .stream()
            .filter(i -> i.getItem().getName().equals("simple_inventory_test_item_a"))
            .findFirst()
            .get();

        final var request = new UpdateInventoryItemRequest();
        request.setQuantity(20);

        final var response = client
            .target(format("%s/inventory/simple/%s", apiRoot, inventoryItem.getId()))
            .request()
            .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
            .put(Entity.entity(request, APPLICATION_JSON))
            .readEntity(InventoryItem.class);

        assertNotNull(response.getId());
        assertEquals(response.getItem().getId(), itemA.getId());
        assertEquals(response.getUser().getId(), userContext.getUser().getId());
        assertEquals(response.getQuantity(), 20);
        assertEquals(response.getPriority(), 0);

    }

    @Test(dependsOnMethods = {"testUpdateInventoryItems"}, dataProvider = "getUserContext")
    public void testCheckNonEmptyInventory(final ClientContext userContext) {

        final var items = client
            .target(apiRoot + "/inventory/simple")
            .request()
            .header("Authorization", format("Bearer %s", userContext.getSessionSecret()))
            .get()
            .readEntity(InventoryItemPagination.class);

        assertEquals(items.getObjects().size(), 1);

        final var item = items.getObjects().get(0);
        assertEquals(item.getPriority(), 0);
        assertEquals(item.getQuantity(), 20);
        assertEquals(item.getUser().getId(), userContext.getUser().getId());
        assertEquals(item.getItem().getId(), itemA.getId());

    }

    @Test(dependsOnMethods = "testCheckNonEmptyInventory", dataProvider = "getUserContext")
    public void testDeleteInventoryItem(final ClientContext userContext) {

        final var items = client
            .target(apiRoot + "/inventory/simple")
            .request()
            .header("Authorization", format("Bearer %s", userContext.getSessionSecret()))
            .get()
            .readEntity(InventoryItemPagination.class);

        assertFalse(items.getObjects().isEmpty(), "Expected non-empty object response.");

        final var inventoryItem = items.getObjects()
            .stream()
            .filter(i -> i.getItem().getName().equals("simple_inventory_test_item_a"))
            .findFirst()
            .get();

        final var response = client
            .target(format("%s/inventory/simple/%s", apiRoot, inventoryItem.getId()))
            .request()
            .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
            .delete();

        assertEquals(response.getStatus(), 204);

    }

}
