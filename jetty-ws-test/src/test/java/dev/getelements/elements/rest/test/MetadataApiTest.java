package dev.getelements.elements.rest.test;

import dev.getelements.elements.rest.test.model.MetadataPagination;
import dev.getelements.elements.sdk.model.metadata.CreateMetadataRequest;
import dev.getelements.elements.sdk.model.metadata.Metadata;
import dev.getelements.elements.sdk.model.metadata.UpdateMetadataRequest;
import dev.getelements.elements.sdk.model.schema.CreateMetadataSpecRequest;
import dev.getelements.elements.sdk.model.schema.MetadataSpec;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.util.MetadataSpecBuilder;
import dev.getelements.elements.sdk.model.util.PaginationWalker;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static dev.getelements.elements.rest.test.TestUtils.TEST_API_ROOT;
import static dev.getelements.elements.sdk.model.Headers.SESSION_SECRET;
import static dev.getelements.elements.sdk.model.schema.MetadataSpecPropertyType.*;
import static jakarta.ws.rs.client.Entity.entity;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static java.lang.String.format;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

public class MetadataApiTest {

    @Factory
    public Object[] getTests() {
        return new Object[] {
                TestUtils.getInstance().getTestFixture(MetadataApiTest.class)
        };
    }

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private Client client;

    @Inject
    private ClientContext superUserClientContext;

    private Metadata workingMetadata;

    private MetadataSpec workingMetadataSpec;

    @BeforeClass
    public void createSuperUser() {
        superUserClientContext
                .createSuperuser("metadataAdmin")
                .createSession();
    }

    @Test
    public void createMetadataSpec() {

        final var properties = MetadataSpecBuilder.propertiesBuilder()
                .property()
                .name("test_a").type(STRING).displayName("Test A")
                .endProperty()
                .property()
                .name("test_b").type(NUMBER).displayName("Test B")
                .endProperty()
                .endProperties();

        final var request = new CreateMetadataSpecRequest();
        request.setName("test_metadata_spec_for_metadata");
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
        assertEquals("test_metadata_spec_for_metadata", metadataSpec.getName());
        assertEquals(properties, metadataSpec.getProperties());

        workingMetadataSpec = metadataSpec;

    }

    @Test(groups = "create", dependsOnMethods = "createMetadataSpec")
    public void testCreateMetadata() {

        final var metadata = Map.of(
            "ListKey", List.of("value1", "value2", "value3"),
            "StringKey", "value4",
            "IntKey", 5,
            "MapKey", Map.of("name", "test")
        );

        final var createMetadataRequest = new CreateMetadataRequest();
        createMetadataRequest.setName("test_metadata");
        createMetadataRequest.setMetadataSpec(workingMetadataSpec);
        createMetadataRequest.setMetadata(metadata);
        createMetadataRequest.setAccessLevel(User.Level.SUPERUSER);

        final var response = client
                .target(apiRoot + "/metadata")
                .request()
                .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
                .post(entity(createMetadataRequest, APPLICATION_JSON));

        assertEquals(response.getStatus(), 200);

        final var metadataObject = response.readEntity(Metadata.class);

        assertNotNull(metadataObject);
        assertNotNull(metadataObject.getId());
        assertEquals(User.Level.SUPERUSER, metadataObject.getAccessLevel());
        assertEquals("test_metadata", metadataObject.getName());
        assertEquals(metadata, metadataObject.getMetadata());
        assertEquals(workingMetadataSpec, metadataObject.getMetadataSpec());

        workingMetadata = metadataObject;

    }

    @Test(groups = "update", dependsOnGroups = "create")
    public void testUpdateMetadata() {

        final var metadata = Map.of(
            "ListKey", List.of("value12", "value21", "value33"),
            "StringKey", "value41321",
            "IntKey", 123123,
            "MapKey", Map.of("name", "test2")
        );

        final var request = new UpdateMetadataRequest();

        request.setMetadataSpec(workingMetadataSpec);
        request.setMetadata(metadata);
        request.setAccessLevel(User.Level.USER);

        final var response = client
                .target(format("%s/metadata/%s", apiRoot, workingMetadata.getId()))
                .request()
                .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
                .put(entity(request, APPLICATION_JSON));

        assertEquals(response.getStatus(), 200);

        final var metadataObject = response.readEntity(Metadata.class);

        assertNotNull(metadataObject);
        assertNotNull(metadataObject.getId());
        assertEquals(User.Level.USER, metadataObject.getAccessLevel());
        assertEquals("test_metadata", metadataObject.getName());
        assertEquals(metadata, metadataObject.getMetadata());
        assertEquals(workingMetadataSpec, metadataObject.getMetadataSpec());

        workingMetadata = metadataObject;

    }

    @Test(groups = "fetch")
    public void testGetBogusSpec() {

        final var response = client
                .target(format("%s/metadata/asdf", apiRoot))
                .request()
                .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
                .get();

        assertEquals(response.getStatus(), 404);

    }

    @Test(groups = "fetch", dependsOnGroups = "update")
    public void testGetMetadata() {

        final var response = client
                .target(format("%s/metadata/%s", apiRoot, workingMetadata.getId()))
                .request()
                .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
                .get();

        assertEquals(response.getStatus(), 200);

        final var metadata = response.readEntity(Metadata.class);
        assertEquals(workingMetadata, metadata);

    }

    @Test(groups = "fetch", dependsOnGroups = "update")
    public void testGetMetadatas() {

        final PaginationWalker.WalkFunction<Metadata> walkFunction = (offset, count) -> {

            final var response = client
                    .target(format("%s/metadata?offset=%d&count=%d",
                            apiRoot,
                            offset, count)
                    )
                    .request()
                    .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
                    .get();

            assertEquals(200, response.getStatus());
            return response.readEntity(MetadataPagination.class);

        };

        final var specs = new PaginationWalker().toList(walkFunction);
        assertTrue(specs.contains(workingMetadata));

    }
//
//    @Test(groups = "fetch", dependsOnGroups = "update")
//    public void testGetJsonSchema() {
//
//        final var response = client
//                .target(format("%s/metadata/%s/schema.json", apiRoot, workingMetadata.getName()))
//                .request()
//                .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
//                .get();
//
//        assertEquals(response.getStatus(), 200);
//
//    }

//    @Test(groups = "fetch", dependsOnGroups = "update")
//    public void testGetEditorSchema() {
//
//        final var response = client
//                .target(format("%s/metadata/%s/editor.json", apiRoot, workingMetadata.getName()))
//                .request()
//                .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
//                .get();
//
//        assertEquals(response.getStatus(), 200);
//
//    }

    @Test(groups = "delete", dependsOnGroups = "fetch")
    public void testDeleteMetadata() {

        final var response = client
                .target(format("%s/metadata/%s", apiRoot, workingMetadata.getId()))
                .request()
                .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
                .delete();

        assertEquals(204, response.getStatus());

    }

    @Test(groups = "postDelete", dependsOnGroups = "delete")
    public void testDoubleDelete() {

        final var response = client
                .target(format("%s/metadata/%s", apiRoot, workingMetadata.getId()))
                .request()
                .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
                .delete();

        assertEquals(404, response.getStatus());

    }

}
