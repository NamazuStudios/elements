package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.model.goods.CreateItemRequest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import java.util.HashMap;
import java.util.List;

import static com.namazustudios.socialengine.rest.TestUtils.TEST_API_ROOT;
import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.testng.Assert.assertEquals;

public class InventoryApiTest {

    @Factory
    public static Object[] getTests() {
        return new Object[] {
            TestUtils.getInstance().getXodusTest(InventoryApiTest.class),
            TestUtils.getInstance().getUnixFSTest(InventoryApiTest.class)
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

    @Test
    public void createUsers() {
        user0.createUser("user0").createSession();
        user1.createUser("user1").createSession();
        superUser.createSuperuser("admin").createSession();
    }

    @Test(dependsOnMethods = "createUsers")
    public void createDigitalGoods() {

        final var aRequest = new CreateItemRequest();
        aRequest.setName("test_item_a");
        aRequest.setDisplayName("Test Item A");
        aRequest.setDescription("Test Item A (More)");
        aRequest.setTags(List.of("item_a", "item"));

        final var aMetadata = new HashMap<String, Object>();
        aMetadata.put("test", "item_a");
        aRequest.setMetadata(aMetadata);

        final var bRequest = new CreateItemRequest();
        bRequest.setName("test_item_b");
        bRequest.setDisplayName("Test Item B");
        bRequest.setDescription("Test Item B (More)");
        bRequest.setTags(List.of("item_b", "item"));

        final var bMetadata = new HashMap<String, Object>();
        bMetadata.put("test", "item_b");
        bRequest.setMetadata(bMetadata);

        createItem(aRequest);
        createItem(bRequest);
    }

    private void createItem(final CreateItemRequest createItemRequest) {

        final var response = client
            .target(apiRoot + "/item")
            .request()
            .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
            .post(Entity.entity(createItemRequest, APPLICATION_JSON));

        assertEquals(response.getStatus(), 200);

    }

    @DataProvider
    public Object[][] getUserContext() {
        return new Object[][] {
            new Object[] {user0},
            new Object[] {user1},
        };
    }

    @Test(dependsOnMethods = {"createUsers", "createDigitalGoods"}, dataProvider = "getUserContext")
    public void createCreatUserInventory(final ClientContext userContext) {
        
    }

}
