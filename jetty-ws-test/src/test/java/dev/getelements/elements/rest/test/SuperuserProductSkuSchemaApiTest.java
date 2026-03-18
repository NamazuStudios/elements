package dev.getelements.elements.rest.test;

import dev.getelements.elements.rest.test.model.ProductSkuSchemaPagination;
import dev.getelements.elements.sdk.model.goods.ProductSkuSchema;
import dev.getelements.elements.sdk.model.util.PaginationWalker;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import static dev.getelements.elements.rest.test.TestUtils.TEST_API_ROOT;
import static dev.getelements.elements.sdk.model.Headers.SESSION_SECRET;
import static java.lang.String.format;
import static jakarta.ws.rs.client.Entity.entity;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.testng.Assert.*;

public class SuperuserProductSkuSchemaApiTest {

    @Factory
    public static Object[] getTests() {
        return new Object[] {
            TestUtils.getInstance().getTestFixture(SuperuserProductSkuSchemaApiTest.class)
        };
    }

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private Client client;

    @Inject
    private ClientContext superUserClientContext;

    private ProductSkuSchema working;

    private String schemaEndpoint() {
        return apiRoot + "/product/sku/schema";
    }

    @BeforeClass
    public void setup() {
        superUserClientContext.createSuperuser("productSkuSchemaAdmin").createSession();
    }

    @Test(groups = "create")
    public void testCreateProductSkuSchema() {

        final var schema = new ProductSkuSchema(null, "com.test.payment.provider");

        final var response = client
            .target(schemaEndpoint())
            .request()
            .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
            .post(entity(schema, APPLICATION_JSON));

        assertEquals(response.getStatus(), 201);

        final var created = response.readEntity(ProductSkuSchema.class);
        assertNotNull(created);
        assertNotNull(created.id());
        assertEquals("com.test.payment.provider", created.schema());

        working = created;
    }

    @Test(groups = "create", dependsOnMethods = "testCreateProductSkuSchema")
    public void testCreateProductSkuSchemaIsIdempotent() {

        final var schema = new ProductSkuSchema(null, "com.test.payment.provider");

        final var response = client
            .target(schemaEndpoint())
            .request()
            .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
            .post(entity(schema, APPLICATION_JSON));

        assertEquals(201, response.getStatus());

        final var created = response.readEntity(ProductSkuSchema.class);
        assertNotNull(created);
        assertEquals(working.id(), created.id());
        assertEquals(working.schema(), created.schema());
    }

    @Test(groups = "fetch", dependsOnGroups = "create")
    public void testGetProductSkuSchemaById() {

        final var response = client
            .target(format("%s/%s", schemaEndpoint(), working.id()))
            .request()
            .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
            .get();

        assertEquals(200, response.getStatus());

        final var fetched = response.readEntity(ProductSkuSchema.class);
        assertEquals(working, fetched);
    }

    @Test(groups = "fetch", dependsOnGroups = "create")
    public void testGetBogusProductSkuSchema() {

        final var response = client
            .target(format("%s/000000000000000000000000", schemaEndpoint()))
            .request()
            .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
            .get();

        assertEquals(404, response.getStatus());
    }

    @Test(groups = "fetch", dependsOnGroups = "create")
    public void testListProductSkuSchemas() {

        final PaginationWalker.WalkFunction<ProductSkuSchema> walkFunction = (offset, count) -> {
            final var response = client
                .target(format("%s?offset=%d&count=%d", schemaEndpoint(), offset, count))
                .request()
                .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
                .get();
            assertEquals(200, response.getStatus());
            return response.readEntity(ProductSkuSchemaPagination.class);
        };

        final var schemas = new PaginationWalker().toList(walkFunction);
        assertTrue(schemas.stream().anyMatch(s -> s.id().equals(working.id())));
    }

    @Test(groups = "delete", dependsOnGroups = "fetch")
    public void testDeleteProductSkuSchema() {

        final var response = client
            .target(format("%s/%s", schemaEndpoint(), working.id()))
            .request()
            .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
            .delete();

        assertEquals(204, response.getStatus());
    }

    @Test(groups = "postDelete", dependsOnGroups = "delete")
    public void testGetDeletedProductSkuSchema() {

        final var response = client
            .target(format("%s/%s", schemaEndpoint(), working.id()))
            .request()
            .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
            .get();

        assertEquals(404, response.getStatus());
    }

}
