package dev.getelements.elements.rest;

import dev.getelements.elements.dao.MetadataSpecDao;
import dev.getelements.elements.model.ErrorResponse;
import dev.getelements.elements.model.schema.CreateMetadataSpecRequest;
import dev.getelements.elements.model.schema.MetadataSpec;
import dev.getelements.elements.model.schema.MetadataSpecProperty;
import dev.getelements.elements.model.schema.UpdateMetadataSpecRequest;
import dev.getelements.elements.rest.model.MetadataSpecPagination;
import dev.getelements.elements.util.PaginationWalker;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static dev.getelements.elements.Headers.SESSION_SECRET;
import static dev.getelements.elements.Headers.SOCIALENGINE_SESSION_SECRET;
import static dev.getelements.elements.exception.ErrorCode.FORBIDDEN;
import static dev.getelements.elements.rest.TestUtils.TEST_API_ROOT;
import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.testng.Assert.*;
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

    @Inject
    private ClientContext userClientContext;

    @Inject
    private MetadataSpecDao metadataSpecDao;


    @DataProvider
    public Object[][] getAuthHeader() {
        return new Object[][] {
            new Object[] { SESSION_SECRET },
            new Object[] { SOCIALENGINE_SESSION_SECRET }
        };
    }

    @BeforeClass
    public void createUser() {
        superUserClientContext
            .createSuperuser("metadataSpecAdmin")
            .createSession();
        userClientContext
                .createUser("metadataSpecUser")
                .createSession();
    }

    @Test(dataProvider = "getAuthHeader")
    public void testCreateAndDeleteMetadataSpec(final String authHeader) {
        final var request = new CreateMetadataSpecRequest();
        request.setName("New Token"+ (new Date()).getTime());
        List<TemplateTab> tabs = new ArrayList<>() ;
        Map<String, MetadataSpecProperty> fields = new HashMap<>();
        MetadataSpecProperty field = new MetadataSpecProperty();
        field.setName("field1");
        fields.put("field1", field);
        TemplateTab tab = new TemplateTab("tab1",fields);
        tab.setTabOrder(1);
        tabs.add(tab);
        TemplateTab tab2 = new TemplateTab("tab2",fields);
        tab.setTabOrder(2);
        tabs.add(tab2);
        request.setProperties(tabs);

        MetadataSpec metadataSpec = client
            .target(apiRoot + "/schema/metadata_spec")
            .request()
            .header(authHeader, superUserClientContext.getSessionSecret())
            .post(Entity.entity(request, APPLICATION_JSON))
            .readEntity(MetadataSpec.class);

        assertNotNull(metadataSpec);
        assertNotNull(metadataSpec.getId());
        assertEquals(metadataSpec.getName(), request.getName());
        assertEquals(metadataSpec.getTabs().get(0).getName(), tab.getName());
        assertEquals(metadataSpec.getTabs().get(0).getTabOrder(), tab.getTabOrder());
        assertEquals(metadataSpec.getTabs().get(1).getName(), tab2.getName());
        assertEquals(metadataSpec.getTabs().get(1).getTabOrder(), tab2.getTabOrder());

        Response response = client
                .target(apiRoot + "/schema/metadata_spec/" + metadataSpec.getId())
                .request()
                .header(authHeader, superUserClientContext.getSessionSecret())
                .delete();

        assertEquals(response.getStatus(), 204);
    }

    @Test(dataProvider = "getAuthHeader")
    public void testGetMetadataSpec(final String authHeader) {
        final var request = new CreateMetadataSpecRequest();
        String specName = "New Metadata Spec" + (new Date()).getTime();
        request.setName(specName);
        List<TemplateTab> tabs = new ArrayList<>() ;
        Map<String, MetadataSpecProperty> fields = new HashMap<>();
        MetadataSpecProperty field = new MetadataSpecProperty();
        field.setName("field1");
        fields.put("field", field);
        TemplateTab tab = new TemplateTab("tab1",fields);
        tab.setTabOrder(1);
        tabs.add(tab);
        request.setProperties(tabs);

        Response response = client
            .target(apiRoot + "/schema/metadata_spec")
            .request()
            .header(authHeader, superUserClientContext.getSessionSecret())
            .post(Entity.entity(request, APPLICATION_JSON));

        var created = response.readEntity(MetadataSpec.class);

        assertEquals(response.getStatus(), 200);

        //get Spec by ID
        var metadataSpec = client
            .target(apiRoot + "/schema/metadata_spec/" + created.getId())
            .request()
            .header(authHeader, superUserClientContext.getSessionSecret())
            .get()
            .readEntity(MetadataSpec.class);

        assertNotNull(metadataSpec);
        assertNotNull(metadataSpec.getId());
        assertEquals(metadataSpec.getName(), request.getName());
        assertEquals(metadataSpec.getTabs().get(0).getName(), tab.getName());
        assertEquals(metadataSpec.getTabs().get(0).getTabOrder(), tab.getTabOrder());

        //get Spec by Name
        var metadataSpec2 = client
                .target(apiRoot + "/schema/metadata_spec/" + specName)
                .request()
                .header(authHeader, superUserClientContext.getSessionSecret())
                .get()
                .readEntity(MetadataSpec.class);

        assertNotNull(metadataSpec2);
        assertNotNull(metadataSpec2.getId());
        assertEquals(metadataSpec2.getName(), request.getName());
        assertEquals(metadataSpec2.getTabs().get(0).getName(), tab.getName());
        assertEquals(metadataSpec2.getTabs().get(0).getTabOrder(), tab.getTabOrder());


        response = client
                .target(apiRoot + "/schema/metadata_spec/" + metadataSpec.getId())
                .request()
                .header(authHeader, superUserClientContext.getSessionSecret())
                .delete();

        assertEquals(response.getStatus(), 204);

    }

    @Test(dataProvider = "getAuthHeader")
    public void testUpdateMetadataSpec(final String authHeader) {

        final var request = new CreateMetadataSpecRequest();
        String specName = "New Metadata Spec" + (new Date()).getTime();
        request.setName(specName);
        List<TemplateTab> tabs = new ArrayList<>() ;
        Map<String, MetadataSpecProperty> fields = new HashMap<>();
        MetadataSpecProperty field = new MetadataSpecProperty();
        field.setName("field1");
        fields.put("field1", field);
        TemplateTab tab = new TemplateTab("tab1",fields);
        tab.setTabOrder(1);
        tabs.add(tab);
        request.setProperties(tabs);

        MetadataSpec metadataSpec = client
            .target(apiRoot + "/schema/metadata_spec")
            .request()
            .header(authHeader, superUserClientContext.getSessionSecret())
            .post(Entity.entity(request, APPLICATION_JSON))
            .readEntity(MetadataSpec.class);

        assertNotNull(metadataSpec);
        assertNotNull(metadataSpec.getId());
        assertEquals(metadataSpec.getName(), request.getName());
        assertEquals(metadataSpec.getTabs().get(0).getName(), tab.getName());
        assertEquals(metadataSpec.getTabs().get(0).getTabOrder(), tab.getTabOrder());

        UpdateMetadataSpecRequest updateRequest = new UpdateMetadataSpecRequest();
        updateRequest.setName("Updated Token");
        tabs = new ArrayList<>() ;
        fields = new HashMap<>();
        field = new MetadataSpecProperty();
        field.setName("field2");
        fields.put("field1", field);
        tab = new TemplateTab("tab2",fields);
        tab.setTabOrder(2);
        tabs.add(tab);
        updateRequest.setProperties(tabs);

        var updatedMetadataSpec = client
            .target(apiRoot + "/schema/metadata_spec/" + metadataSpec.getId())
            .request()
            .header(authHeader, superUserClientContext.getSessionSecret())
            .put(Entity.entity(updateRequest, APPLICATION_JSON))
            .readEntity(MetadataSpec.class);

        assertNotNull(updatedMetadataSpec);
        assertNotNull(updatedMetadataSpec.getId());
        assertEquals(updatedMetadataSpec.getName(), updateRequest.getName());
        assertEquals(updatedMetadataSpec.getTabs().get(0).getName(), tab.getName());
        assertEquals(updatedMetadataSpec.getTabs().get(0).getTabOrder(), tab.getTabOrder());

        var response = client
            .target(apiRoot + "/schema/metadata_spec/" + updatedMetadataSpec.getId())
            .request()
            .header(authHeader, superUserClientContext.getSessionSecret())
            .delete();

        assertEquals(response.getStatus(), 204);

    }

    @Test
    public void testGetMetadataSpecs() {

        final var called = new AtomicBoolean();

        final PaginationWalker.WalkFunction<MetadataSpec> walkFunction = (offset, count) -> {
            final var response = client.target(format("%s/schema/metadata_spec?offset=%d&count=%d",
                    apiRoot,
                    offset,
                    count)
            )
            .request()
            .header("Authorization", format("Bearer %s", superUserClientContext.getSessionSecret()))
            .get(MetadataSpecPagination.class);
            called.set(true);
            return response;
        };

        new PaginationWalker().forEach(walkFunction, metadataSpec -> {});
        assertTrue(called.get());

    }

    @Test(dataProvider = "getAuthHeader")
    public void testNormalUserRestrictionAccess(final String authHeader) {

        final var request = new CreateMetadataSpecRequest();
        String specName = "New Metadata Spec" + (new Date()).getTime();
        request.setName(specName);
        List<TemplateTab> tabs = new ArrayList<>() ;
        Map<String, MetadataSpecProperty> fields = new HashMap<>();

        MetadataSpecProperty field = new MetadataSpecProperty();
        field.setName("field1");
        fields.put("field1", field);
        TemplateTab tab = new TemplateTab("tab1",fields);
        tabs.add(tab);
        request.setProperties(tabs);

        final var response = client
                .target(apiRoot + "/schema/metadata_spec")
                .request()
                .header(authHeader, userClientContext.getSessionSecret())
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        final int status = response.getStatus();
        assertEquals(403, status);

        final var error = response.readEntity(ErrorResponse.class);
        assertEquals(FORBIDDEN.toString(), error.getCode());


        try {
            var responseGet = client.target(format("%s/schema/metadata_spec?offset=%d&count=%d",
                            apiRoot,
                            0,
                            30)
                    )
                    .request()
                    .header(authHeader, userClientContext.getSessionSecret())
                    .get(MetadataSpecPagination.class);
        }catch(ForbiddenException e) {
            assertEquals(403, e.getResponse().getStatus());
        }


    }

}
