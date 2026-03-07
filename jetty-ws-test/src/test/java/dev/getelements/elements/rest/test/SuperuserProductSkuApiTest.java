package dev.getelements.elements.rest.test;

import dev.getelements.elements.rest.test.model.ProductSkuPagination;
import dev.getelements.elements.sdk.model.goods.ProductSku;
import dev.getelements.elements.sdk.model.goods.ProductSkuReward;
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

public class SuperuserProductSkuApiTest {

    private static final String TEST_SCHEMA = "com.getelements.test.iapsku";
    private static final String TEST_PRODUCT_ID = "test.product.integration";

    @Factory
    public Object[] getTests() {
        return new Object[]{
                TestUtils.getInstance().getTestFixture(SuperuserProductSkuApiTest.class)
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

    private ProductSku working;

    @BeforeClass
    public void setup() {
        superUserClientContext
                .createSuperuser("productSkuAdmin")
                .createSession();

        userClientContext
                .createUser("productSkuUser")
                .createSession();
    }

    // -----------------------------------------------------------------------
    // CREATE
    // -----------------------------------------------------------------------

    @Test(groups = "create_product_sku")
    public void testCreateProductSku() {
        final var body = new ProductSku(
                null,
                TEST_SCHEMA,
                TEST_PRODUCT_ID,
                List.of(
                        new ProductSkuReward("test-item-1", 3),
                        new ProductSkuReward("test-item-2", null)
                ));

        final var response = client
                .target(apiRoot + "/product/sku")
                .request()
                .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
                .post(entity(body, APPLICATION_JSON));

        assertEquals(response.getStatus(), 201);

        working = response.readEntity(ProductSku.class);

        assertNotNull(working);
        assertNotNull(working.id());
        assertEquals(working.schema(), TEST_SCHEMA);
        assertEquals(working.productId(), TEST_PRODUCT_ID);
        assertEquals(working.rewards().size(), 2);
        assertEquals(working.rewards().get(0).itemId(), "test-item-1");
        assertEquals(working.rewards().get(0).quantity(), 3);
        assertEquals(working.rewards().get(1).itemId(), "test-item-2");
    }

    @Test(groups = "create_product_sku")
    public void testCreateProductSkuForbiddenForUser() {
        final var body = new ProductSku(null, TEST_SCHEMA, "user.should.fail",
                List.of(new ProductSkuReward("item", 1)));

        final var response = client
                .target(apiRoot + "/product/sku")
                .request()
                .header(SESSION_SECRET, userClientContext.getSessionSecret())
                .post(entity(body, APPLICATION_JSON));

        assertEquals(response.getStatus(), 403);
    }

    // -----------------------------------------------------------------------
    // FETCH
    // -----------------------------------------------------------------------

    @Test(groups = "fetch_product_sku", dependsOnGroups = "create_product_sku")
    public void testGetProductSkuById() {
        final var response = client
                .target(format("%s/product/sku/%s", apiRoot, working.id()))
                .request()
                .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
                .get();

        assertEquals(response.getStatus(), 200);
        assertEquals(response.readEntity(ProductSku.class), working);
    }

    @Test(groups = "fetch_product_sku", dependsOnGroups = "create_product_sku")
    public void testGetProductSkuBySchemaAndProductId() {
        final var response = client
                .target(format("%s/product/sku/%s/%s", apiRoot, TEST_SCHEMA, TEST_PRODUCT_ID))
                .request()
                .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
                .get();

        assertEquals(response.getStatus(), 200);
        assertEquals(response.readEntity(ProductSku.class), working);
    }

    @Test(groups = "fetch_product_sku", dependsOnGroups = "create_product_sku")
    public void testListProductSkus() {
        final var response = client
                .target(format("%s/product/sku?offset=0&count=100", apiRoot))
                .request()
                .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
                .get();

        assertEquals(response.getStatus(), 200);

        final var page = response.readEntity(ProductSkuPagination.class);
        assertNotNull(page);
        assertTrue(page.getObjects().stream().anyMatch(s -> s.id().equals(working.id())));
    }

    @Test(groups = "fetch_product_sku", dependsOnGroups = "create_product_sku")
    public void testListProductSkusBySchema() {
        final var response = client
                .target(format("%s/product/sku?schema=%s&offset=0&count=100", apiRoot, TEST_SCHEMA))
                .request()
                .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
                .get();

        assertEquals(response.getStatus(), 200);

        final var page = response.readEntity(ProductSkuPagination.class);
        assertNotNull(page);
        assertTrue(page.getObjects().stream().anyMatch(s -> s.id().equals(working.id())));
        assertTrue(page.getObjects().stream().allMatch(s -> TEST_SCHEMA.equals(s.schema())));
    }

    @Test(groups = "fetch_product_sku", dependsOnGroups = "create_product_sku")
    public void testGetProductSkuForbiddenForUser() {
        final var response = client
                .target(format("%s/product/sku/%s", apiRoot, working.id()))
                .request()
                .header(SESSION_SECRET, userClientContext.getSessionSecret())
                .get();

        assertEquals(response.getStatus(), 403);
    }

    // -----------------------------------------------------------------------
    // UPDATE
    // -----------------------------------------------------------------------

    @Test(groups = "update_product_sku", dependsOnGroups = "fetch_product_sku")
    public void testUpdateProductSku() {
        final var updated = new ProductSku(
                null,
                working.schema(),
                working.productId(),
                List.of(new ProductSkuReward("test-item-1", 99)));

        final var response = client
                .target(format("%s/product/sku/%s", apiRoot, working.id()))
                .request()
                .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
                .put(entity(updated, APPLICATION_JSON));

        assertEquals(response.getStatus(), 200);

        final var result = response.readEntity(ProductSku.class);
        assertNotNull(result);
        assertEquals(result.id(), working.id());
        assertEquals(result.rewards().size(), 1);
        assertEquals(result.rewards().get(0).quantity(), 99);

        working = result;
    }

    // -----------------------------------------------------------------------
    // DELETE
    // -----------------------------------------------------------------------

    @Test(groups = "delete_product_sku", dependsOnGroups = "update_product_sku")
    public void testDeleteProductSku() {
        final var response = client
                .target(format("%s/product/sku/%s", apiRoot, working.id()))
                .request()
                .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
                .delete();

        assertEquals(response.getStatus(), 204);
    }

    @Test(groups = "postDelete_product_sku", dependsOnGroups = "delete_product_sku")
    public void testGetDeletedProductSkuReturns404() {
        final var response = client
                .target(format("%s/product/sku/%s", apiRoot, working.id()))
                .request()
                .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
                .get();

        assertEquals(response.getStatus(), 404);
    }

}
