package dev.getelements.elements.rest.test;

import dev.getelements.elements.rest.test.model.ProductBundlePagination;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.goods.ProductBundle;
import dev.getelements.elements.sdk.model.util.PaginationWalker;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import static dev.getelements.elements.rest.test.ClientContext.CONTEXT_APPLICATION;
import static dev.getelements.elements.rest.test.TestUtils.TEST_API_ROOT;
import static dev.getelements.elements.sdk.model.Headers.SESSION_SECRET;
import static java.lang.String.format;
import static jakarta.ws.rs.client.Entity.entity;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.testng.Assert.*;

public class SuperuserProductBundleApiTest {

    @Factory
    public static Object[] getTests() {
        return new Object[] {
            TestUtils.getInstance().getTestFixture(SuperuserProductBundleApiTest.class)
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
    @Named(CONTEXT_APPLICATION)
    private Application contextApplication;

    private ProductBundle working;

    private String bundleEndpoint() {
        return apiRoot + "/product/bundle";
    }

    @BeforeClass
    public void setup() {
        superUserClientContext.createSuperuser("productBundleAdmin").createSession();
    }

    @Test(groups = "create")
    public void testCreateProductBundle() {

        final var bundle = new ProductBundle();
        bundle.setSchema("com.test.store");
        bundle.setProductId("test_product_001");
        bundle.setDisplayName("Test Bundle");
        bundle.setDescription("A test product bundle");
        bundle.setDisplay(true);
        bundle.setApplication(contextApplication);

        final var response = client
            .target(bundleEndpoint())
            .request()
            .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
            .post(entity(bundle, APPLICATION_JSON));

        assertEquals(response.getStatus(), 201);

        final var created = response.readEntity(ProductBundle.class);
        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("com.test.store", created.getSchema());
        assertEquals("test_product_001", created.getProductId());
        assertEquals("Test Bundle", created.getDisplayName());
        assertTrue(created.isDisplay());
        assertNotNull(created.getApplication());

        working = created;
    }

    @Test(groups = "fetch", dependsOnGroups = "create")
    public void testGetProductBundleById() {

        final var response = client
            .target(format("%s/%s", bundleEndpoint(), working.getId()))
            .request()
            .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
            .get();

        assertEquals(200, response.getStatus());

        final var fetched = response.readEntity(ProductBundle.class);
        assertEquals(working, fetched);
    }

    @Test(groups = "fetch", dependsOnGroups = "create")
    public void testGetProductBundleByKey() {

        final var appName = contextApplication.getName();
        final var response = client
            .target(format("%s/%s/%s/%s", bundleEndpoint(), appName, working.getSchema(), working.getProductId()))
            .request()
            .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
            .get();

        assertEquals(200, response.getStatus());

        final var fetched = response.readEntity(ProductBundle.class);
        assertEquals(working.getId(), fetched.getId());
        assertEquals(working.getSchema(), fetched.getSchema());
        assertEquals(working.getProductId(), fetched.getProductId());
    }

    @Test(groups = "fetch", dependsOnGroups = "create")
    public void testGetBogusProductBundle() {

        final var response = client
            .target(format("%s/000000000000000000000000", bundleEndpoint()))
            .request()
            .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
            .get();

        assertEquals(404, response.getStatus());
    }

    @Test(groups = "fetch", dependsOnGroups = "create")
    public void testListProductBundles() {

        final PaginationWalker.WalkFunction<ProductBundle> walkFunction = (offset, count) -> {
            final var response = client
                .target(format("%s?offset=%d&count=%d", bundleEndpoint(), offset, count))
                .request()
                .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
                .get();
            assertEquals(200, response.getStatus());
            return response.readEntity(ProductBundlePagination.class);
        };

        final var bundles = new PaginationWalker().toList(walkFunction);
        assertTrue(bundles.stream().anyMatch(b -> b.getId().equals(working.getId())));
    }

    @Test(groups = "fetch", dependsOnGroups = "create")
    public void testListProductBundlesByApplication() {

        final var response = client
            .target(format("%s?applicationNameOrId=%s&offset=0&count=20",
                    bundleEndpoint(), contextApplication.getName()))
            .request()
            .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
            .get();

        assertEquals(200, response.getStatus());

        final var page = response.readEntity(ProductBundlePagination.class);
        assertNotNull(page);
        assertTrue(page.getObjects().stream().anyMatch(b -> b.getId().equals(working.getId())));
    }

    @Test(groups = "update", dependsOnGroups = "fetch")
    public void testUpdateProductBundle() {

        final var updated = new ProductBundle();
        updated.setId(working.getId());
        updated.setSchema(working.getSchema());
        updated.setProductId(working.getProductId());
        updated.setApplication(working.getApplication());
        updated.setDisplayName("Updated Bundle Name");
        updated.setDescription("Updated description");
        updated.setDisplay(false);

        final var response = client
            .target(format("%s/%s", bundleEndpoint(), working.getId()))
            .request()
            .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
            .put(entity(updated, APPLICATION_JSON));

        assertEquals(200, response.getStatus());

        final var result = response.readEntity(ProductBundle.class);
        assertNotNull(result);
        assertEquals("Updated Bundle Name", result.getDisplayName());
        assertEquals("Updated description", result.getDescription());
        assertFalse(result.isDisplay());

        working = result;
    }

    @Test(groups = "delete", dependsOnGroups = "update")
    public void testDeleteProductBundle() {

        final var response = client
            .target(format("%s/%s", bundleEndpoint(), working.getId()))
            .request()
            .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
            .delete();

        assertEquals(204, response.getStatus());
    }

    @Test(groups = "postDelete", dependsOnGroups = "delete")
    public void testGetDeletedProductBundle() {

        final var response = client
            .target(format("%s/%s", bundleEndpoint(), working.getId()))
            .request()
            .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
            .get();

        assertEquals(404, response.getStatus());
    }

}
