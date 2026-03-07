package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.dao.IapSkuDao;
import dev.getelements.elements.sdk.model.exception.DuplicateException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.iap.IapSku;
import dev.getelements.elements.sdk.model.iap.IapSkuReward;
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
public class MongoIapSkuDaoTest {

    private static final String TEST_SCHEMA = "com.getelements.test.iap";

    private IapSkuDao iapSkuDao;

    private IapSku created;

    private String deletedId;

    @BeforeClass
    public void setUp() {
        // Clean up any leftover state from a previous interrupted run
        try {
            final var existing = getIapSkuDao().getIapSku(TEST_SCHEMA, "test.product.a");
            getIapSkuDao().deleteIapSku(existing.id());
        } catch (NotFoundException ignored) {}
    }

    @AfterClass
    public void tearDown() {
        if (created != null) {
            try {
                getIapSkuDao().deleteIapSku(created.id());
            } catch (NotFoundException ignored) {}
        }
    }

    // -----------------------------------------------------------------------
    // CREATE
    // -----------------------------------------------------------------------

    @Test(groups = "create")
    public void testCreateIapSku() {
        final var sku = new IapSku(
                null,
                TEST_SCHEMA,
                "test.product.a",
                List.of(
                        new IapSkuReward("item-fungible-1", 5),
                        new IapSkuReward("item-distinct-2", null)
                ));

        created = getIapSkuDao().createIapSku(sku);

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

    @Test(groups = "create", dependsOnMethods = "testCreateIapSku", expectedExceptions = DuplicateException.class)
    public void testCreateDuplicateThrows() {
        final var duplicate = new IapSku(null, TEST_SCHEMA, "test.product.a",
                List.of(new IapSkuReward("some-item", 1)));
        getIapSkuDao().createIapSku(duplicate);
    }

    // -----------------------------------------------------------------------
    // READ
    // -----------------------------------------------------------------------

    @Test(groups = "read", dependsOnGroups = "create")
    public void testGetIapSkuById() {
        final var result = getIapSkuDao().getIapSku(created.id());
        assertNotNull(result);
        assertEquals(result, created);
    }

    @Test(groups = "read", dependsOnGroups = "create")
    public void testGetIapSkuBySchemaAndProductId() {
        final var result = getIapSkuDao().getIapSku(TEST_SCHEMA, "test.product.a");
        assertNotNull(result);
        assertEquals(result, created);
    }

    @Test(groups = "read", dependsOnGroups = "create")
    public void testGetIapSkusBySchema() {
        final var page = getIapSkuDao().getIapSkus(TEST_SCHEMA, 0, 20);
        assertNotNull(page);
        assertTrue(page.getObjects().stream().anyMatch(s -> s.id().equals(created.id())));
    }

    @Test(groups = "read", dependsOnGroups = "create")
    public void testGetIapSkus() {
        final var page = getIapSkuDao().getIapSkus(0, 100);
        assertNotNull(page);
        assertTrue(page.getObjects().stream().anyMatch(s -> s.id().equals(created.id())));
    }

    @Test(groups = "read", expectedExceptions = NotFoundException.class)
    public void testGetByInvalidIdThrows() {
        getIapSkuDao().getIapSku("not-a-valid-object-id");
    }

    @Test(groups = "read", expectedExceptions = NotFoundException.class)
    public void testGetByUnknownSchemaThrows() {
        getIapSkuDao().getIapSku("com.nonexistent.schema", "nonexistent.product");
    }

    // -----------------------------------------------------------------------
    // UPDATE
    // -----------------------------------------------------------------------

    @Test(groups = "update", dependsOnGroups = "read")
    public void testUpdateIapSku() {
        final var newRewards = List.of(new IapSkuReward("item-fungible-1", 10));
        final var toUpdate = new IapSku(created.id(), created.schema(), created.productId(), newRewards);

        final var result = getIapSkuDao().updateIapSku(toUpdate);

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

    @Test(groups = "delete", dependsOnGroups = "update")
    public void testDeleteIapSku() {
        deletedId = created.id();
        getIapSkuDao().deleteIapSku(deletedId);
        // Mark as already cleaned up so tearDown doesn't try again
        created = null;
    }

    @Test(groups = "delete", dependsOnMethods = "testDeleteIapSku", expectedExceptions = NotFoundException.class)
    public void testGetDeletedIapSkuThrows() {
        getIapSkuDao().getIapSku(deletedId);
    }

    @Test(groups = "delete", dependsOnGroups = "update", expectedExceptions = NotFoundException.class)
    public void testDeleteNonExistentThrows() {
        // Use a syntactically valid but non-existent ObjectId
        getIapSkuDao().deleteIapSku("aaaaaaaaaaaaaaaaaaaaaaaa");
    }

    public IapSkuDao getIapSkuDao() {
        return iapSkuDao;
    }

    @Inject
    public void setIapSkuDao(IapSkuDao iapSkuDao) {
        this.iapSkuDao = iapSkuDao;
    }

}
