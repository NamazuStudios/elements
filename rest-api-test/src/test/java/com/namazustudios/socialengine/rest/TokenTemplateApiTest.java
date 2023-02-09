package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.model.ErrorResponse;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.ElementsSmartContract;
import com.namazustudios.socialengine.model.blockchain.PatchSmartContractRequest;
import com.namazustudios.socialengine.model.schema.template.*;
import com.namazustudios.socialengine.rest.model.ElementsSmartContractPagination;
import com.namazustudios.socialengine.rest.model.MetadataSpecPagination;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Stream;

import static com.namazustudios.socialengine.Headers.SESSION_SECRET;
import static com.namazustudios.socialengine.Headers.SOCIALENGINE_SESSION_SECRET;
import static com.namazustudios.socialengine.exception.ErrorCode.FORBIDDEN;
import static com.namazustudios.socialengine.rest.TestUtils.TEST_API_ROOT;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static java.util.stream.IntStream.range;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.AssertJUnit.assertEquals;

public class TokenTemplateApiTest {

    @Factory
    public Object[] getTests() {
        return new Object[] {
                TestUtils.getInstance().getXodusTest(TokenTemplateApiTest.class),
                TestUtils.getInstance().getUnixFSTest(TokenTemplateApiTest.class)
        };
    }

    private  String specId;

    private String contractId;

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private Client client;

    @Inject
    private ClientContext user0;

    @Inject
    private ClientContext user1;

    @Inject
    private ClientContext superUserClientContext;


    @Test
    public void setupTestItems() throws Exception {
        superUserClientContext
                .createSuperuser("tokenTemplateAdmin")
                .createSession();

        final Pagination<MetadataSpec> specPagination = client
                .target(apiRoot + "/schema/metadata_spec")
                .queryParam("offset", 0)
                .queryParam("count", 20)
                .request()
                .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
                .buildGet()
                .submit(MetadataSpecPagination.class)
                .get();

        MetadataSpec metadataSpec = specPagination!=null && specPagination.getObjects().size() > 0?specPagination.getObjects().get(0):null;

        if (metadataSpec == null){

            final var request = new CreateMetadataSpecRequest();
            request.setName("New Token");
            List<TemplateTab> tabs = new ArrayList<>() ;
            Map<String, TemplateTabField> fields = new HashMap<>();
            TemplateTabField field = new TemplateTabField();
            field.setName("field1");
            fields.put("field1", field);
            TemplateTab tab = new TemplateTab("tab1",fields);
            tab.setTabOrder(1);
            tabs.add(tab);
            request.setTabs(tabs);

            metadataSpec = client
                    .target(apiRoot + "/schema/metadata_spec")
                    .request()
                    .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
                    .post(Entity.entity(request, APPLICATION_JSON))
                    .readEntity(MetadataSpec.class);
        }
        this.specId = metadataSpec.getId();


        final Pagination<ElementsSmartContract> contractPagination = client
                .target(apiRoot + "/blockchain/neo/contract")
                .queryParam("count", 20)
                .queryParam("search", "")
                .request()
                .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
                .buildGet()
                .submit(ElementsSmartContractPagination.class)
                .get();

        ElementsSmartContract contract =
                contractPagination!=null && contractPagination.getObjects().size() > 0
                        ? contractPagination.getObjects().get(0)
                        : null;

        if (contract == null){
            final var request = new PatchSmartContractRequest();
            request.setDisplayName("smartcontract-apitest");
            request.setScriptHash("1");
            request.setBlockchain("NEO");

            contract = client
                    .target(apiRoot + "/blockchain/neo/contract")
                    .request()
                    .header("X-HTTP-Method-Override", "PATCH")
                    .header("Authorization", format("Bearer %s", superUserClientContext.getSessionSecret()))
                    .post(Entity.entity(request, APPLICATION_JSON))
                    .readEntity(ElementsSmartContract.class);
        }
        contractId = contract.getId();

    }

    @DataProvider
    public Object[][] getAuthHeader() {
        return new Object[][] {
                new Object[] { SESSION_SECRET },
                new Object[] { SOCIALENGINE_SESSION_SECRET }
        };
    }

    @Test(dependsOnMethods = "setupTestItems", dataProvider = "getAuthHeader")
    public void testCreateAndDeleteTokenTemplate(final String authHeader) {

        String tokenTemplateName = "TokenTemplateTest-" + randomUUID().toString();

        final var request = new CreateTokenTemplateRequest();
        request.setUserId(superUserClientContext.getUser().getId());
        request.setName(tokenTemplateName);
        request.setContractId(this.contractId);
        request.setMetadataSpecId(this.specId);
        request.setDisplayName("Token Template");


        TokenTemplate tokenTemplate = client
                .target(apiRoot + "/schema/token_template")
                .request()
                .header(authHeader, superUserClientContext.getSessionSecret())
                .post(Entity.entity(request, APPLICATION_JSON))
                .readEntity(TokenTemplate.class);

        assertNotNull(tokenTemplate);
        assertNotNull(tokenTemplate.getId());
        assertEquals(tokenTemplate.getUser().getId(), superUserClientContext.getUser().getId());
        assertEquals(tokenTemplate.getName(), tokenTemplateName);

        String req = "/schema/token_template/" + tokenTemplate.getId();

        Response response = client
                .target(apiRoot + req)
                .request()
                .header(authHeader, superUserClientContext.getSessionSecret())
                .delete();

        assertEquals(response.getStatus(), 204);
    }
//
    @Test(dependsOnMethods = "setupTestItems", dataProvider = "getAuthHeader")
    public void testGetTokenTemplate(final String authHeader) {

        String tokenTemplateName = "TokenTemplateTest-" + randomUUID().toString();

        final var request = new CreateTokenTemplateRequest();
        request.setUserId(superUserClientContext.getUser().getId());
        request.setName(tokenTemplateName);
        request.setContractId(this.contractId);
        request.setMetadataSpecId(this.specId);
        request.setDisplayName("Token Template");

        Response response = client
                .target(apiRoot + "/schema/token_template")
                .request()
                .header(authHeader, superUserClientContext.getSessionSecret())
                .post(Entity.entity(request, APPLICATION_JSON));

        assertEquals(response.getStatus(), 200);

        var tokenTemplate = client
                .target(apiRoot + "/schema/token_template/" + tokenTemplateName)
                .request()
                .header(authHeader, superUserClientContext.getSessionSecret())
                .get()
                .readEntity(TokenTemplate.class);

        assertNotNull(tokenTemplate);
        assertNotNull(tokenTemplate.getId());
        assertEquals(tokenTemplate.getUser().getId(), superUserClientContext.getUser().getId());
        assertEquals(tokenTemplate.getName(), tokenTemplateName);

        String req = "/schema/token_template/" + tokenTemplate.getId();

        response = client
                .target(apiRoot + req)
                .request()
                .header(authHeader, superUserClientContext.getSessionSecret())
                .delete();

        assertEquals(response.getStatus(), 204);
    }
//
    @Test(dependsOnMethods = "setupTestItems", dataProvider = "getAuthHeader")
    public void testUpdateTokenTemplate(final String authHeader) {

        String tokenTemplateName = "TokenTemplateTest-" + randomUUID().toString();

        final var request = new CreateTokenTemplateRequest();
        request.setUserId(superUserClientContext.getUser().getId());
        request.setName(tokenTemplateName);
        request.setContractId(this.contractId);
        request.setMetadataSpecId(this.specId);
        request.setDisplayName("Token Template");

        TokenTemplate tokenTemplate = client
                .target(apiRoot + "/schema/token_template")
                .request()
                .header(authHeader, superUserClientContext.getSessionSecret())
                .post(Entity.entity(request, APPLICATION_JSON))
                .readEntity(TokenTemplate.class);

        assertNotNull(tokenTemplate);
        assertNotNull(tokenTemplate.getId());
        assertEquals(tokenTemplate.getUser().getId(), superUserClientContext.getUser().getId());
        assertEquals(tokenTemplate.getName(), tokenTemplateName);

        UpdateTokenTemplateRequest updateRequest = new UpdateTokenTemplateRequest();
        updateRequest.setUserId(superUserClientContext.getUser().getId());
        updateRequest.setName(tokenTemplateName);
        updateRequest.setContractId(this.contractId);
        updateRequest.setMetadataSpecId(this.specId);
        updateRequest.setDisplayName("Token Template 2");

        var updatedTokenTemplate = client
                .target(apiRoot + "/schema/token_template/" + tokenTemplate.getId())
                .request()
                .header(authHeader, superUserClientContext.getSessionSecret())
                .put(Entity.entity(updateRequest, APPLICATION_JSON))
                .readEntity(TokenTemplate.class);

        assertNotNull(updatedTokenTemplate);
        assertNotNull(updatedTokenTemplate.getId());
        assertEquals(updatedTokenTemplate.getUser().getId(), superUserClientContext.getUser().getId());
        assertEquals(updatedTokenTemplate.getDisplayName(), "Token Template 2");

        Response response = client
                .target(apiRoot + "/schema/token_template/" + tokenTemplate.getId())
                .request()
                .header(authHeader, superUserClientContext.getSessionSecret())
                .delete();

        assertEquals(response.getStatus(), 204);
    }



    @BeforeClass
    public void testSetup() {

        user0.createUser("save_data_user_0")
                .createProfile("save_data_profile_0")
                .createSession();

        user1.createUser("save_data_user_1")
                .createProfile("save_data_user_1")
                .createSession();

    }

    @DataProvider
    public Object[][] getClientContexts() {
        return new Object[][] { {user0}, {user1} };
    }

    @DataProvider
    public Object[][] getClientContextsForOpposingUsers() {
        return Stream.concat(
                range(0, 10).mapToObj(slot -> new Object[]{user0, user1}),
                range(0, 10).mapToObj(slot -> new Object[]{user1, user0})
        ).toArray(Object[][]::new);
    }

    @Test(dependsOnMethods = "setupTestItems", dataProvider = "getClientContextsForOpposingUsers")
    public void testCreateUserSaveTokenTemplateFailsAcrossUsers(final ClientContext context,
                                                           final ClientContext other) {

        String tokenTemplateName = "TokenTemplateTest-" + randomUUID().toString();

        final var request = new CreateTokenTemplateRequest();
        request.setUserId(superUserClientContext.getUser().getId());
        request.setName(tokenTemplateName);
        request.setContractId(this.contractId);
        request.setMetadataSpecId(this.specId);
        request.setDisplayName("Token Template");


        final var response = client
                .target(apiRoot + "/schema/token_template")
                .request()
                .header("Authorization", format("Bearer %s", context.getSessionSecret()))
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        final int status = response.getStatus();
        assertEquals(403, status);

        final var error = response.readEntity(ErrorResponse.class);
        assertEquals(FORBIDDEN.toString(), error.getCode());

    }

    @Test(dependsOnMethods = "setupTestItems", dataProvider = "getClientContexts")
    public void testCheckedUpdateUserSaveTokenTemplate(final ClientContext context) {
        String tokenTemplateName = "TokenTemplateTest-" + randomUUID().toString();

        final var request = new CreateTokenTemplateRequest();
        request.setUserId(context.getUser().getId());
        request.setName(tokenTemplateName);
        request.setContractId(this.contractId);
        request.setMetadataSpecId(this.specId);
        request.setDisplayName("Token Template");

        TokenTemplate tokenTemplate = client
                .target(apiRoot + "/schema/token_template")
                .request()
                .header("Authorization", format("Bearer %s", context.getSessionSecret()))
                .post(Entity.entity(request, APPLICATION_JSON))
                .readEntity(TokenTemplate.class);

        assertNotNull(tokenTemplate);
        assertNotNull(tokenTemplate.getId());
        assertEquals(tokenTemplate.getUser().getId(), context.getUser().getId());
        assertEquals(tokenTemplate.getName(), tokenTemplateName);

        String updatedTokenTemplateName = "TokenTemplateTest-" + randomUUID().toString();
        UpdateTokenTemplateRequest updateRequest = new UpdateTokenTemplateRequest();
        updateRequest.setName(updatedTokenTemplateName);
        updateRequest.setUserId(context.getUser().getId());
        updateRequest.setContractId(this.contractId);
        updateRequest.setMetadataSpecId(this.specId);
        updateRequest.setDisplayName("Updated Token Template");


        var updatedTokenTemplate = client
                .target(apiRoot + "/schema/token_template/" + tokenTemplate.getId())
                .request()
                .header("Authorization", format("Bearer %s", context.getSessionSecret()))
                .put(Entity.entity(updateRequest, APPLICATION_JSON))
                .readEntity(TokenTemplate.class);

        assertNotNull(updatedTokenTemplate);
        assertNotNull(updatedTokenTemplate.getId());
        assertEquals(updatedTokenTemplate.getUser().getId(), context.getUser().getId());
        assertEquals(updatedTokenTemplate.getName(), updatedTokenTemplateName);

        Response response = client
                .target(apiRoot + "/schema/token_template/" + tokenTemplate.getId())
                .request()
                .header("Authorization", format("Bearer %s", context.getSessionSecret()))
                .delete();

        assertEquals(response.getStatus(), 204);

    }
}
