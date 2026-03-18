package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.dao.ProductSkuSchemaDao;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.goods.ProductSkuSchema;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import static org.testng.Assert.*;

@Guice(modules = IntegrationTestModule.class)
public class MongoProductSkuSchemaDaoTest {

    private static final int SCHEMA_COUNT = 5;

    private ProductSkuSchemaDao productSkuSchemaDao;

    /** Schemas created during testCreateProductSkuSchema, shared with dependent tests. */
    private final Map<String, ProductSkuSchema> createdSchemas = new ConcurrentHashMap<>();

    @DataProvider
    public Object[][] schemaIterations() {
        return IntStream.range(0, SCHEMA_COUNT)
                .mapToObj(i -> new Object[]{i})
                .toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] createdSchemaEntries() {
        return createdSchemas.entrySet().stream()
                .map(e -> new Object[]{e.getKey(), e.getValue()})
                .toArray(Object[][]::new);
    }

    // -------------------------------------------------------------------------
    // Create
    // -------------------------------------------------------------------------

    @Test(dataProvider = "schemaIterations", groups = "create")
    public void testCreateProductSkuSchema(final int iteration) {

        final var toCreate = new ProductSkuSchema(null, "com.test.schema." + iteration + "." + UUID.randomUUID());
        final var created = getProductSkuSchemaDao().createProductSkuSchema(toCreate);

        assertNotNull(created.id(), "id should be assigned on create");
        assertEquals(created.schema(), toCreate.schema());

        createdSchemas.put(created.id(), created);
    }

    @Test(dataProvider = "schemaIterations", dependsOnMethods = "testCreateProductSkuSchema")
    public void testCreateDuplicateProductSkuSchemaIsIdempotent(final int iteration) {
        // Re-use the schema string from a previously created entry — should return the same record
        final var existing = createdSchemas.values().stream()
                .skip(iteration)
                .findFirst()
                .orElseThrow();
        final var result = getProductSkuSchemaDao().createProductSkuSchema(new ProductSkuSchema(null, existing.schema()));
        assertEquals(result.id(), existing.id(), "createProductSkuSchema should return the existing record on duplicate");
        assertEquals(result.schema(), existing.schema());
    }

    // -------------------------------------------------------------------------
    // List
    // -------------------------------------------------------------------------

    @Test(dependsOnMethods = "testCreateProductSkuSchema")
    public void testListProductSkuSchemas() {
        final var page = getProductSkuSchemaDao().getProductSkuSchemas(0, 100);
        assertTrue(page.getTotal() >= SCHEMA_COUNT,
                "Should return at least the schemas we created");
        for (final var schema : page.getObjects()) {
            assertNotNull(schema.id());
            assertNotNull(schema.schema());
        }
    }

    // -------------------------------------------------------------------------
    // Ensure (idempotent upsert)
    // -------------------------------------------------------------------------

    @Test(dependsOnMethods = "testCreateProductSkuSchema")
    public void testEnsureProductSkuSchemaCreatesNew() {
        final var uniqueSchema = "com.test.ensure." + UUID.randomUUID();
        final var result = getProductSkuSchemaDao().ensureProductSkuSchema(uniqueSchema);

        assertNotNull(result.id(), "ensureProductSkuSchema should assign an id for new schemas");
        assertEquals(result.schema(), uniqueSchema);

        // Clean up
        getProductSkuSchemaDao().deleteProductSkuSchema(result.id());
    }

    @Test(dependsOnMethods = "testCreateProductSkuSchema")
    public void testEnsureProductSkuSchemaIsIdempotent() {
        final var uniqueSchema = "com.test.ensure.idempotent." + UUID.randomUUID();

        final var first  = getProductSkuSchemaDao().ensureProductSkuSchema(uniqueSchema);
        final var second = getProductSkuSchemaDao().ensureProductSkuSchema(uniqueSchema);

        assertNotNull(first.id());
        assertEquals(first.id(), second.id(), "ensureProductSkuSchema should return the same record on repeat calls");
        assertEquals(first.schema(), second.schema());

        // Clean up
        getProductSkuSchemaDao().deleteProductSkuSchema(first.id());
    }

    // -------------------------------------------------------------------------
    // Delete
    // -------------------------------------------------------------------------

    @Test(dependsOnMethods = {"testListProductSkuSchemas", "testCreateDuplicateProductSkuSchemaIsIdempotent"},
          dataProvider = "createdSchemaEntries")
    public void testDeleteProductSkuSchema(final String id, final ProductSkuSchema schema) {
        getProductSkuSchemaDao().deleteProductSkuSchema(id);
        createdSchemas.remove(id);
    }

    @Test(dependsOnMethods = "testDeleteProductSkuSchema",
          expectedExceptions = NotFoundException.class)
    public void testDeleteNonExistentProductSkuSchema() {
        getProductSkuSchemaDao().deleteProductSkuSchema(new ObjectId().toHexString());
    }

    @Test(dependsOnMethods = "testDeleteProductSkuSchema",
          expectedExceptions = NotFoundException.class)
    public void testDeleteInvalidIdProductSkuSchema() {
        getProductSkuSchemaDao().deleteProductSkuSchema("not-a-valid-id");
    }

    // -------------------------------------------------------------------------
    // Guice injection
    // -------------------------------------------------------------------------

    public ProductSkuSchemaDao getProductSkuSchemaDao() {
        return productSkuSchemaDao;
    }

    @Inject
    public void setProductSkuSchemaDao(final ProductSkuSchemaDao productSkuSchemaDao) {
        this.productSkuSchemaDao = productSkuSchemaDao;
    }

}
