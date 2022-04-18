package com.namazustudios.socialengine.rest;


import com.namazustudios.socialengine.dao.TokenTemplateDao;
import com.namazustudios.socialengine.model.ErrorResponse;
import com.namazustudios.socialengine.model.blockchain.template.*;
import com.namazustudios.socialengine.rest.model.TokenTemplatePagination;
import com.namazustudios.socialengine.util.PaginationWalker;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.namazustudios.socialengine.Headers.SESSION_SECRET;
import static com.namazustudios.socialengine.Headers.SOCIALENGINE_SESSION_SECRET;
import static com.namazustudios.socialengine.exception.ErrorCode.FORBIDDEN;
import static com.namazustudios.socialengine.rest.TestUtils.TEST_API_ROOT;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.testng.Assert.*;
import static org.testng.AssertJUnit.assertEquals;

public class TokenTemplateApiTest {

    @Factory
    public Object[] getTests() {
        return new Object[] {
            TestUtils.getInstance().getXodusTest(TokenTemplateApiTest.class),
            TestUtils.getInstance().getUnixFSTest(TokenTemplateApiTest.class)
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
    private TokenTemplateDao tokenTemplateDao;


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
            .createSuperuser("tokenTemplateAdmin")
            .createSession();
        userClientContext
                .createUser("tokenTemplateUser")
                .createSession();
    }

    @Test(dataProvider = "getAuthHeader")
    public void testCreateAndDeleteTokenTemplate(final String authHeader) {
        final var request = new CreateTokenTemplateRequest();
        request.setTokenName("New Token");
        request.setContractId("uu1234");
        List<TemplateTab> tabs = new ArrayList<>() ;
        List<TemplateTabField> fields = new ArrayList<>();
        TemplateTabField field = new TemplateTabField();
        field.setName("field1");
        field.setContent("Test");
        fields.add(field);
        TemplateTab tab = new TemplateTab("tab1",fields);
        tab.setTabOrder(1);
        tabs.add(tab);
        TemplateTab tab2 = new TemplateTab("tab2",fields);
        tab.setTabOrder(2);
        tabs.add(tab2);
        request.setTabs(tabs);

        TokenTemplate tokenTemplate = client
            .target(apiRoot + "/blockchain/token/template")
            .request()
            .header(authHeader, superUserClientContext.getSessionSecret())
            .post(Entity.entity(request, APPLICATION_JSON))
            .readEntity(TokenTemplate.class);

        assertNotNull(tokenTemplate);
        assertNotNull(tokenTemplate.getId());
        assertEquals(tokenTemplate.getTokenName(), request.getTokenName());
        assertEquals(tokenTemplate.getContractId(), request.getContractId());
        assertEquals(tokenTemplate.getTabs().get(0).getName(), tab.getName());
        assertEquals(tokenTemplate.getTabs().get(0).getTabOrder(), tab.getTabOrder());
        assertEquals(tokenTemplate.getTabs().get(1).getName(), tab2.getName());
        assertEquals(tokenTemplate.getTabs().get(1).getTabOrder(), tab2.getTabOrder());

        Response response = client
                .target(apiRoot + "/blockchain/token/template/" + tokenTemplate.getId())
                .request()
                .header(authHeader, superUserClientContext.getSessionSecret())
                .delete();

        assertEquals(response.getStatus(), 204);
    }

    @Test(dataProvider = "getAuthHeader")
    public void testGetTokenTemplate(final String authHeader) {
        final var request = new CreateTokenTemplateRequest();
        request.setTokenName("New Token");
        request.setContractId("uu1234");
        List<TemplateTab> tabs = new ArrayList<>() ;
        List<TemplateTabField> fields = new ArrayList<>();
        TemplateTabField field = new TemplateTabField();
        field.setName("field1");
        field.setContent("Test");
        fields.add(field);
        TemplateTab tab = new TemplateTab("tab1",fields);
        tab.setTabOrder(1);
        tabs.add(tab);
        request.setTabs(tabs);

        Response response = client
            .target(apiRoot + "/blockchain/token/template")
            .request()
            .header(authHeader, superUserClientContext.getSessionSecret())
            .post(Entity.entity(request, APPLICATION_JSON));

        var created = response.readEntity(TokenTemplate.class);

        assertEquals(response.getStatus(), 200);

        var tokenTemplate = client
            .target(apiRoot + "/blockchain/token/template/" + created.getId())
            .request()
            .header(authHeader, superUserClientContext.getSessionSecret())
            .get()
            .readEntity(TokenTemplate.class);

        assertNotNull(tokenTemplate);
        assertNotNull(tokenTemplate.getId());
        assertEquals(tokenTemplate.getTokenName(), request.getTokenName());
        assertEquals(tokenTemplate.getContractId(), request.getContractId());
        assertEquals(tokenTemplate.getTabs().get(0).getName(), tab.getName());
        assertEquals(tokenTemplate.getTabs().get(0).getTabOrder(), tab.getTabOrder());

        response = client
                .target(apiRoot + "/blockchain/token/template/" + tokenTemplate.getId())
                .request()
                .header(authHeader, superUserClientContext.getSessionSecret())
                .delete();

        assertEquals(response.getStatus(), 204);

    }

    @Test(dataProvider = "getAuthHeader")
    public void testUpdateTokenTemplate(final String authHeader) {

        final var request = new CreateTokenTemplateRequest();
        request.setTokenName("New Token");
        request.setContractId("uu1234");
        List<TemplateTab> tabs = new ArrayList<>() ;
        List<TemplateTabField> fields = new ArrayList<>();
        TemplateTabField field = new TemplateTabField();
        field.setName("field1");
        field.setContent("Test");
        fields.add(field);
        TemplateTab tab = new TemplateTab("tab1",fields);
        tab.setTabOrder(1);
        tabs.add(tab);
        request.setTabs(tabs);

        TokenTemplate tokenTemplate = client
            .target(apiRoot + "/blockchain/token/template")
            .request()
            .header(authHeader, superUserClientContext.getSessionSecret())
            .post(Entity.entity(request, APPLICATION_JSON))
            .readEntity(TokenTemplate.class);

        assertNotNull(tokenTemplate);
        assertNotNull(tokenTemplate.getId());
        assertEquals(tokenTemplate.getTokenName(), request.getTokenName());
        assertEquals(tokenTemplate.getContractId(), request.getContractId());
        assertEquals(tokenTemplate.getTabs().get(0).getName(), tab.getName());
        assertEquals(tokenTemplate.getTabs().get(0).getTabOrder(), tab.getTabOrder());

        UpdateTokenTemplateRequest updateRequest = new UpdateTokenTemplateRequest();
        updateRequest.setTokenName("Updated Token");
        updateRequest.setContractId("uu6789");
        tabs = new ArrayList<>() ;
        fields = new ArrayList<>();
        field = new TemplateTabField();
        field.setName("field2");
        field.setContent("Test");
        fields.add(field);
        tab = new TemplateTab("tab2",fields);
        tab.setTabOrder(2);
        tabs.add(tab);
        updateRequest.setTabs(tabs);

        var updatedTokenTemplate = client
            .target(apiRoot + "/blockchain/token/template/" + tokenTemplate.getId())
            .request()
            .header(authHeader, superUserClientContext.getSessionSecret())
            .put(Entity.entity(updateRequest, APPLICATION_JSON))
            .readEntity(TokenTemplate.class);

        assertNotNull(updatedTokenTemplate);
        assertNotNull(updatedTokenTemplate.getId());
        assertEquals(updatedTokenTemplate.getTokenName(), updateRequest.getTokenName());
        assertEquals(updatedTokenTemplate.getContractId(), updateRequest.getContractId());
        assertEquals(updatedTokenTemplate.getTabs().get(0).getName(), tab.getName());
        assertEquals(updatedTokenTemplate.getTabs().get(0).getTabOrder(), tab.getTabOrder());

        var response = client
            .target(apiRoot + "/blockchain/token/template/" + updatedTokenTemplate.getId())
            .request()
            .header(authHeader, superUserClientContext.getSessionSecret())
            .delete();

        assertEquals(response.getStatus(), 204);

    }

    @Test
    public void testGetTokenTemplates() {

        final var called = new AtomicBoolean();

        final PaginationWalker.WalkFunction<TokenTemplate> walkFunction = (offset, count) -> {
            final var response = client.target(format("%s/blockchain/token/template?offset=%d&count=%d",
                    apiRoot,
                    offset,
                    count)
            )
            .request()
            .header("Authorization", format("Bearer %s", superUserClientContext.getSessionSecret()))
            .get(TokenTemplatePagination.class);
            called.set(true);
            return response;
        };

        new PaginationWalker().forEach(walkFunction, tokenTemplate -> {});
        assertTrue(called.get());

    }

    @Test(dataProvider = "getAuthHeader")
    public void testNormalUserRestrictionAccess(final String authHeader) {

        final var request = new CreateTokenTemplateRequest();
        List<TemplateTab> tabs = new ArrayList<>() ;
        List<TemplateTabField> fields = new ArrayList<>();
        TemplateTabField field = new TemplateTabField();
        field.setName("field1");
        field.setContent("Test");
        fields.add(field);
        TemplateTab tab = new TemplateTab("tab1",fields);
        tabs.add(tab);
        request.setTabs(tabs);

        final var response = client
                .target(apiRoot + "/blockchain/token/template")
                .request()
                .header(authHeader, userClientContext.getSessionSecret())
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        final int status = response.getStatus();
        assertEquals(403, status);

        final var error = response.readEntity(ErrorResponse.class);
        assertEquals(FORBIDDEN.toString(), error.getCode());


        try {
            var responseGet = client.target(format("%s/blockchain/token/template?offset=%d&count=%d",
                            apiRoot,
                            0,
                            30)
                    )
                    .request()
                    .header(authHeader, userClientContext.getSessionSecret())
                    .get(TokenTemplatePagination.class);
        }catch(ForbiddenException e) {
            assertEquals(403, e.getResponse().getStatus());
        }


    }

}
