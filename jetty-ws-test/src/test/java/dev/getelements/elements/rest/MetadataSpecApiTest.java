package dev.getelements.elements.rest;

import dev.getelements.elements.model.schema.CreateMetadataSpecRequest;
import dev.getelements.elements.model.schema.MetadataSpec;
import dev.getelements.elements.model.schema.UpdateMetadataSpecRequest;
import dev.getelements.elements.rest.model.MetadataSpecPagination;
import dev.getelements.elements.util.MetadataSpecBuilder;
import dev.getelements.elements.util.PaginationWalker;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;

import static dev.getelements.elements.Headers.SESSION_SECRET;
import static dev.getelements.elements.model.schema.MetadataSpecPropertyType.*;
import static dev.getelements.elements.rest.TestUtils.TEST_API_ROOT;
import static java.lang.String.format;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

public class MetadataSpecApiTest {

    @Factory
    public Object[] getTests() {
        return new Object[] {
            TestUtils.getInstance().getXodusTest(MetadataSpecApiTest.class),
            TestUtils.getInstance().getUnixFSTest(MetadataSpecApiTest.class)
        };
    }

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private Client client;

    @Inject
    private ClientContext superUserClientContext;

    private MetadataSpec working;

    @BeforeClass
    public void createSuperUser() {
        superUserClientContext
            .createSuperuser("metadataSpecAdmin")
            .createSession();
    }

    @Test(groups = "create")
    public void testCreateMetadataSpec() {

        final var properties = MetadataSpecBuilder.propertiesBuilder()
                    .property()
                        .name("test_a").type(STRING).displayName("Test A")
                    .endProperty()
                    .property()
                        .name("test_b").type(NUMBER).displayName("Test B")
                    .endProperty()
                .endProperties();

        final var request = new CreateMetadataSpecRequest();
        request.setName("test_a");
        request.setType(OBJECT);
        request.setProperties(properties);

        final var response = client
            .target(apiRoot + "/metadata_spec")
            .request()
            .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
            .post(entity(request, APPLICATION_JSON));

        assertEquals(response.getStatus(), 200);

        final var metadataSpec = response.readEntity(MetadataSpec.class);

        assertNotNull(metadataSpec);
        assertNotNull(metadataSpec.getId());
        assertEquals(OBJECT, metadataSpec.getType());
        assertEquals("test_a", metadataSpec.getName());
        assertEquals(properties, metadataSpec.getProperties());

        working = metadataSpec;

    }

    @Test(groups = "update", dependsOnGroups = "create")
    public void testUpdateMetadataSpec() {

        final var properties = MetadataSpecBuilder.propertiesBuilder()
                    .property()
                     .name("update_test_a").type(STRING).displayName("Update Test A")
                  .endProperty()
                    .property()
                        .name("update_test_b").type(NUMBER).displayName("Update Test B")
                    .endProperty()
                .endProperties();

        final var request = new UpdateMetadataSpecRequest();
        request.setName("test_a");
        request.setType(OBJECT);
        request.setProperties(properties);

        final var response = client
                .target(format("%s/metadata_spec/%s", apiRoot, working.getId()))
                .request()
                .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
                .put(entity(request, APPLICATION_JSON));

        assertEquals(response.getStatus(), 200);

        final var metadataSpec = response.readEntity(MetadataSpec.class);

        assertNotNull(metadataSpec);
        assertNotNull(metadataSpec.getId());
        assertEquals(OBJECT, metadataSpec.getType());
        assertEquals("test_a", metadataSpec.getName());
        assertEquals(properties, metadataSpec.getProperties());

        working = metadataSpec;

    }

    @Test(groups = "fetch")
    public void testGetBogusSpec() {

        final var response = client
                .target(format("%s/metadata_spec/asdf", apiRoot))
                .request()
                .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
                .get();

        assertEquals(response.getStatus(), 404);

    }

    @Test(groups = "fetch", dependsOnGroups = "update")
    public void testGetMetadataSpec() {

        final var response = client
                .target(format("%s/metadata_spec/%s", apiRoot, working.getId()))
                .request()
                .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
                .get();

        assertEquals(response.getStatus(), 200);

        final var metadataSpec = response.readEntity(MetadataSpec.class);
        assertEquals(working, metadataSpec);

    }

    @Test(groups = "fetch", dependsOnGroups = "update")
    public void testGetMetadataSpecs() {

        final PaginationWalker.WalkFunction<MetadataSpec> walkFunction = (offset, count) -> {

            final var response = client
                    .target(format("%s/metadata_spec?offset=%d&count=%d",
                            apiRoot,
                            offset, count)
                    )
                    .request()
                    .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
                    .get();

            assertEquals(200, response.getStatus());
            return response.readEntity(MetadataSpecPagination.class);

        };

        final var specs = new PaginationWalker().toList(walkFunction);
        assertTrue(specs.contains(working));

    }

    @Test(groups = "delete", dependsOnGroups = "fetch")
    public void testDeleteMetadataSpec() {

        final var response = client
                .target(format("%s/metadata_spec/%s", apiRoot, working.getId()))
                .request()
                .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
                .delete();

        assertEquals(204, response.getStatus());

    }

    @Test(groups = "postDelete", dependsOnGroups = "delete")
    public void testDoubleDelete() {

        final var response = client
                .target(format("%s/metadata_spec/%s", apiRoot, working.getId()))
                .request()
                .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
                .delete();

        assertEquals(404, response.getStatus());

    }

}
