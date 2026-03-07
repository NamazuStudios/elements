package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.dao.ProductSkuDao;
import dev.getelements.elements.sdk.model.exception.DuplicateException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.goods.ProductSku;
import dev.getelements.elements.sdk.model.goods.ProductSkuReward;
import jakarta.inject.Inject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

@Guice(modules = IntegrationTestModule.class)
public class MongoProductSkuDaoTest {

    private static final String TEST_SCHEMA = "com.getelements.test.iap";

    private ProductSkuDao productSkuDao;

    private ProductSku created;

    private String deletedId;

    @BeforeClass
    public void setUp() {
        // Clean up any leftover state from a previous interrupted run
        try {
            final var existing = getProductSkuDao().getProductSku(TEST_SCHEMA, "test.product.a");
            getProductSkuDao().deleteProductSku(existing.id());
        } catch (NotFoundException ignored) {}
    }

    @AfterClass
    public void tearDown() {
        if (created != null) {
            try {
                getProductSkuDao().deleteProductSku(created.id());
            } catch (NotFoundException ignored) {}
        }
    }

    // -----------------------------------------------------------------------
    // CREATE
    // -----------------------------------------------------------------------

    @Test(groups = "create_product_sku")
    public void testCreateProductSku() {
        final var sku = new ProductSku(
                null,
                TEST_SCHEMA,
                "test.product.a",
                List.of(
                        new ProductSkuReward("item-fungible-1", 5),
                        new ProductSkuReward("item-distinct-2", null)
                ));

        created = getProductSkuDao().createProductSku(sku);

        assertNotNull(created);
        assertNotNull(created.id());
        assertEquals(created.schema(), TEST_SCHEMA);
        assertEquals(created.productId(), "test.product.a");
        assertEquals(created.rewards().size(), 2);
        assertEquals(created.rewards().get(0).itemId(), "item-fungible-1");
        assertEquals(created.rewards().get(0).quantity(), 5);
        assertEquals(created.rewards().get(1).itemId(), "item-distinct-2");
        assertNull(created.rewards().get(1).quantity());
    }

    @Test(groups = "create_product_sku", dependsOnMethods = "testCreateProductSku", expectedExceptions = DuplicateException.class)
    public void testCreateDuplicateThrows() {
        final var duplicate = new ProductSku(null, TEST_SCHEMA, "test.product.a",
                List.of(new ProductSkuReward("some-item", 1)));
        getProductSkuDao().createProductSku(duplicate);
    }

    // -----------------------------------------------------------------------
    // READ
    // -----------------------------------------------------------------------

    @Test(groups = "read_product_sku", dependsOnGroups = "create_product_sku")
    public void testGetProductSkuById() {
        final var result = getProductSkuDao().getProductSku(created.id());
        assertNotNull(result);
        assertEquals(result, created);
    }

    @Test(groups = "read_product_sku", dependsOnGroups = "create_product_sku")
    public void testGetProductSkuBySchemaAndProductId() {
        final var result = getProductSkuDao().getProductSku(TEST_SCHEMA, "test.product.a");
        assertNotNull(result);
        assertEquals(result, created);
    }

    @Test(groups = "read_product_sku", dependsOnGroups = "create_product_sku")
    public void testGetProductSkusBySchema() {
        final var page = getProductSkuDao().getProductSkus(TEST_SCHEMA, 0, 20);
        assertNotNull(page);
        assertTrue(page.getObjects().stream().anyMatch(s -> s.id().equals(created.id())));
    }

    @Test(groups = "read_product_sku", dependsOnGroups = "create_product_sku")
    public void testGetProductSkus() {
        final var page = getProductSkuDao().getProductSkus(0, 100);
        assertNotNull(page);
        assertTrue(page.getObjects().stream().anyMatch(s -> s.id().equals(created.id())));
    }

    @Test(groups = "read_product_sku", expectedExceptions = NotFoundException.class)
    public void testGetByInvalidIdThrows() {
        getProductSkuDao().getProductSku("not-a-valid-object-id");
    }

    @Test(groups = "read_product_sku", expectedExceptions = NotFoundException.class)
    public void testGetByUnknownSchemaThrows() {
        getProductSkuDao().getProductSku("com.nonexistent.schema", "nonexistent.product");
    }

    // -----------------------------------------------------------------------
    // UPDATE
    // -----------------------------------------------------------------------

    @Test(groups = "update_product_sku", dependsOnGroups = "read_product_sku")
    public void testUpdateProductSku() {
        final var newRewards = List.of(new ProductSkuReward("item-fungible-1", 10));
        final var toUpdate = new ProductSku(created.id(), created.schema(), created.productId(), newRewards);

        final var result = getProductSkuDao().updateProductSku(toUpdate);

        assertNotNull(result);
        assertEquals(result.id(), created.id());
        assertEquals(result.rewards().size(), 1);
        assertEquals(result.rewards().get(0).quantity(), 10);

        // Refresh created so delete-phase uses up-to-date state
        created = result;
    }

    // -----------------------------------------------------------------------
    // DELETE
    // -----------------------------------------------------------------------

    @Test(groups = "delete_product_sku", dependsOnGroups = "update_product_sku")
    public void testDeleteProductSku() {
        deletedId = created.id();
        getProductSkuDao().deleteProductSku(deletedId);
        // Mark as already cleaned up so tearDown doesn't try again
        created = null;
    }

    @Test(groups = "delete_product_sku", dependsOnMethods = "testDeleteProductSku", expectedExceptions = NotFoundException.class)
    public void testGetDeletedProductSkuThrows() {
        getProductSkuDao().getProductSku(deletedId);
    }

    @Test(groups = "delete_product_sku", dependsOnGroups = "update_product_sku", expectedExceptions = NotFoundException.class)
    public void testDeleteNonExistentThrows() {
        // Use a syntactically valid but non-existent ObjectId
        getProductSkuDao().deleteProductSku("aaaaaaaaaaaaaaaaaaaaaaaa");
    }

    public ProductSkuDao getProductSkuDao() {
        return productSkuDao;
    }

    @Inject
    public void setProductSkuDao(ProductSkuDao productSkuDao) {
        this.productSkuDao = productSkuDao;
    }

}
