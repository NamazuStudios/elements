package dev.getelements.elements.rest.test;

import dev.getelements.elements.rest.test.model.IapSkuPagination;
import dev.getelements.elements.sdk.model.iap.IapSku;
import dev.getelements.elements.sdk.model.iap.IapSkuReward;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.List;

import static dev.getelements.elements.rest.test.TestUtils.TEST_API_ROOT;
import static dev.getelements.elements.sdk.model.Headers.SESSION_SECRET;
import static jakarta.ws.rs.client.Entity.entity;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class SuperuserIapSkuApiTest {

    private static final String TEST_SCHEMA = "com.getelements.test.iapsku";
    private static final String TEST_PRODUCT_ID = "test.product.integration";

    @Factory
    public Object[] getTests() {
        return new Object[]{
                TestUtils.getInstance().getTestFixture(SuperuserIapSkuApiTest.class)
        };
    }

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private Client client;

    @Inject
    private ClientContext superUserClientContext;

    @Inject
    private ClientContext userClientContext;

    private IapSku working;

    @BeforeClass
    public void setup() {
        superUserClientContext
                .createSuperuser("iapSkuAdmin")
                .createSession();

        userClientContext
                .createUser("iapSkuUser")
                .createSession();
    }

    // -----------------------------------------------------------------------
    // CREATE
    // -----------------------------------------------------------------------

    @Test(groups = "create")
    public void testCreateIapSku() {
        final var body = new IapSku(
                null,
                TEST_SCHEMA,
                TEST_PRODUCT_ID,
                List.of(
                        new IapSkuReward("test-item-1", 3),
                        new IapSkuReward("test-item-2", null)
                ));

        final var response = client
                .target(apiRoot + "/iap/sku")
                .request()
                .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
                .post(entity(body, APPLICATION_JSON));

        assertEquals(response.getStatus(), 201);

        working = response.readEntity(IapSku.class);

        assertNotNull(working);
        assertNotNull(working.id());
        assertEquals(working.schema(), TEST_SCHEMA);
        assertEquals(working.productId(), TEST_PRODUCT_ID);
        assertEquals(working.rewards().size(), 2);
        assertEquals(working.rewards().get(0).itemId(), "test-item-1");
        assertEquals(working.rewards().get(0).quantity(), 3);
        assertEquals(working.rewards().get(1).itemId(), "test-item-2");
    }

    @Test(groups = "create")
    public void testCreateIapSkuForbiddenForUser() {
        final var body = new IapSku(null, TEST_SCHEMA, "user.should.fail",
                List.of(new IapSkuReward("item", 1)));

        final var response = client
                .target(apiRoot + "/iap/sku")
                .request()
                .header(SESSION_SECRET, userClientContext.getSessionSecret())
                .post(entity(body, APPLICATION_JSON));

        assertEquals(response.getStatus(), 403);
    }

    // -----------------------------------------------------------------------
    // FETCH
    // -----------------------------------------------------------------------

    @Test(groups = "fetch", dependsOnGroups = "create")
    public void testGetIapSkuById() {
        final var response = client
                .target(format("%s/iap/sku/%s", apiRoot, working.id()))
                .request()
                .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
                .get();

        assertEquals(response.getStatus(), 200);
        assertEquals(response.readEntity(IapSku.class), working);
    }

    @Test(groups = "fetch", dependsOnGroups = "create")
    public void testGetIapSkuBySchemaAndProductId() {
        final var response = client
                .target(format("%s/iap/sku/%s/%s", apiRoot, TEST_SCHEMA, TEST_PRODUCT_ID))
                .request()
                .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
                .get();

        assertEquals(response.getStatus(), 200);
        assertEquals(response.readEntity(IapSku.class), working);
    }

    @Test(groups = "fetch", dependsOnGroups = "create")
    public void testListIapSkus() {
        final var response = client
                .target(format("%s/iap/sku?offset=0&count=100", apiRoot))
                .request()
                .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
                .get();

        assertEquals(response.getStatus(), 200);

        final var page = response.readEntity(IapSkuPagination.class);
        assertNotNull(page);
        assertTrue(page.getObjects().stream().anyMatch(s -> s.id().equals(working.id())));
    }

    @Test(groups = "fetch", dependsOnGroups = "create")
    public void testListIapSkusBySchema() {
        final var response = client
                .target(format("%s/iap/sku?schema=%s&offset=0&count=100", apiRoot, TEST_SCHEMA))
                .request()
                .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
                .get();

        assertEquals(response.getStatus(), 200);

        final var page = response.readEntity(IapSkuPagination.class);
        assertNotNull(page);
        assertTrue(page.getObjects().stream().anyMatch(s -> s.id().equals(working.id())));
        assertTrue(page.getObjects().stream().allMatch(s -> TEST_SCHEMA.equals(s.schema())));
    }

    @Test(groups = "fetch", dependsOnGroups = "create")
    public void testGetIapSkuForbiddenForUser() {
        final var response = client
                .target(format("%s/iap/sku/%s", apiRoot, working.id()))
                .request()
                .header(SESSION_SECRET, userClientContext.getSessionSecret())
                .get();

        assertEquals(response.getStatus(), 403);
    }

    // -----------------------------------------------------------------------
    // UPDATE
    // -----------------------------------------------------------------------

    @Test(groups = "update", dependsOnGroups = "fetch")
    public void testUpdateIapSku() {
        final var updated = new IapSku(
                null,
                working.schema(),
                working.productId(),
                List.of(new IapSkuReward("test-item-1", 99)));

        final var response = client
                .target(format("%s/iap/sku/%s", apiRoot, working.id()))
                .request()
                .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
                .put(entity(updated, APPLICATION_JSON));

        assertEquals(response.getStatus(), 200);

        final var result = response.readEntity(IapSku.class);
        assertNotNull(result);
        assertEquals(result.id(), working.id());
        assertEquals(result.rewards().size(), 1);
        assertEquals(result.rewards().get(0).quantity(), 99);

        working = result;
    }

    // -----------------------------------------------------------------------
    // DELETE
    // -----------------------------------------------------------------------

    @Test(groups = "delete", dependsOnGroups = "update")
    public void testDeleteIapSku() {
        final var response = client
                .target(format("%s/iap/sku/%s", apiRoot, working.id()))
                .request()
                .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
                .delete();

        assertEquals(response.getStatus(), 204);
    }

    @Test(groups = "postDelete", dependsOnGroups = "delete")
    public void testGetDeletedIapSkuReturns404() {
        final var response = client
                .target(format("%s/iap/sku/%s", apiRoot, working.id()))
                .request()
                .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
                .get();

        assertEquals(response.getStatus(), 404);
    }

}
