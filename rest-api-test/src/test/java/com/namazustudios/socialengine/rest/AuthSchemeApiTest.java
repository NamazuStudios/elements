package com.namazustudios.socialengine.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.namazustudios.socialengine.model.auth.*;
import com.namazustudios.socialengine.rt.exception.BadRequestException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import java.util.ArrayList;

import static com.namazustudios.socialengine.Headers.SESSION_SECRET;
import static com.namazustudios.socialengine.Headers.SOCIALENGINE_SESSION_SECRET;
import static com.namazustudios.socialengine.rest.TestUtils.TEST_API_ROOT;
import static com.namazustudios.socialengine.security.AuthorizationHeader.AUTH_HEADER;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.testng.Assert.assertNotNull;

public class AuthSchemeApiTest {

    @Factory
    public Object[] getTests() {
        return new Object[] {
                TestUtils.getInstance().getXodusTest(AuthSchemeApiTest.class),
                TestUtils.getInstance().getUnixFSTest(AuthSchemeApiTest.class)
        };
    }

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private Client client;

    @Inject
    private ClientContext clientContext;

    CreateAuthSchemeResponse createAuthSchemeResponse;

    UpdateAuthSchemeResponse updateAuthSchemeResponse;

    @DataProvider
    public Object[][] getAuthHeader() {
        return new Object[][] {
                new Object[] { AUTH_HEADER, "%s" },
                new Object[] { AUTH_HEADER, "Bearer %s" },
                new Object[] { SESSION_SECRET, "%s" },
                new Object[] { SOCIALENGINE_SESSION_SECRET, "%s" }
        };
    }

    @Test()
    public void createAuthScheme() {
        final var createRequest = new CreateAuthSchemeRequest();

        createRequest.setAud("test_aud");
        createRequest.setUserLevel("SUPERUSER");
        createRequest.setPubKey("test_key");

        createAuthSchemeResponse = client
                .target(apiRoot + "/auth_scheme")
                .request()
                .post(Entity.entity(createRequest, APPLICATION_JSON))
                .readEntity(CreateAuthSchemeResponse.class);

        assertNotNull(createAuthSchemeResponse);
        assertNotNull(createAuthSchemeResponse.publicKey);
    }

    @Test(dependsOnMethods = "createAuthScheme")
    public void updateAuthScheme() {
        var scheme = createAuthSchemeResponse.getScheme();
        var objectMapper = new ObjectMapper();

        AuthScheme authScheme;
        try {
            authScheme = objectMapper.readValue(scheme, AuthScheme.class);
        } catch (JsonProcessingException e) {
            throw new BadRequestException(e);
        }

        var allowedIssuers = new ArrayList<String>();

        final var updateRequest = new UpdateAuthSchemeRequest();
        updateRequest.setAuthSchemeId(authScheme.id);
        updateRequest.setAud("test_aud");
        updateRequest.setPubKey("test_key");
        updateRequest.setRegenerate(false);
        updateRequest.setUserLevel("SUPERUSER");
        updateRequest.setAllowedIssuers(allowedIssuers);

        updateAuthSchemeResponse = client
                .target(apiRoot + "/auth_scheme")
                .request()
                .put(Entity.entity(updateRequest, APPLICATION_JSON))
                .readEntity(UpdateAuthSchemeResponse.class);

        assertNotNull(updateAuthSchemeResponse);
    }
}
