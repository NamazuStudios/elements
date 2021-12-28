package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.model.blockchain.*;
import com.namazustudios.socialengine.model.profile.CreateProfileRequest;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.model.session.SessionCreation;
import com.namazustudios.socialengine.model.session.UsernamePasswordSessionRequest;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.model.user.UserCreateRequest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.namazustudios.socialengine.Headers.SESSION_SECRET;
import static com.namazustudios.socialengine.Headers.SOCIALENGINE_SESSION_SECRET;
import static com.namazustudios.socialengine.rest.TestUtils.*;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.testng.Assert.*;

public class NeoTokenApiTest {

    @Factory
    public Object[] getTests() {
        return new Object[] {
                TestUtils.getInstance().getXodusTest(NeoTokenApiTest.class),
                TestUtils.getInstance().getUnixFSTest(NeoTokenApiTest.class)
        };
    }

    private User user;

    private SessionCreation sessionCreation;

    private final String name = "testuser-name-" + randomUUID().toString();

    private final String email = "testuser-email-" + randomUUID().toString() + "@example.com";

    private final String password = randomUUID().toString();

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private Client client;

    @Inject
    private ClientContext superUserClientContext;

    @DataProvider
    public Object[][] getAuthHeader() {
        return new Object[][] {
                new Object[] { SESSION_SECRET },
                new Object[] { SOCIALENGINE_SESSION_SECRET }
        };
    }

    @Test
    public void createUser() {
        superUserClientContext
                .createSuperuser("tokenAdmin")
                .createSession();
    }

    @DataProvider
    public Object[][] credentialsProvider() {
        return new Object[][] {
                new Object[]{name, password},
                new Object[]{email, password},
                new Object[]{user.getId(), password}
        };
    }

    @Test(dependsOnMethods = "createUser", dataProvider = "getAuthHeader")
    public void testCreateAndDeleteToken(final String authHeader) {

        String tokenName = "TokenTest-" + randomUUID().toString();

        final var request = new CreateNeoTokenRequest();
        request.setToken(newToken(tokenName));
        request.setListed(false);
        request.setContractId("");

        NeoToken neoToken = client
                .target(apiRoot + "/blockchain/neo/token")
                .request()
                .header(authHeader, superUserClientContext.getSessionSecret())
                .post(Entity.entity(request, APPLICATION_JSON))
                .readEntity(NeoToken.class);

        assertNotNull(neoToken);
        assertNotNull(neoToken.getId());
        assertEquals(neoToken.getToken().getName(), tokenName);

        Response response = client
                .target(apiRoot + "/blockchain/neo/token/" + neoToken.getId())
                .request()
                .header(authHeader, superUserClientContext.getSessionSecret())
                .delete();

        assertEquals(response.getStatus(), 204);
    }

    @Test(dependsOnMethods = "createUser", dataProvider = "getAuthHeader")
    public void testGetToken(final String authHeader) {

        String tokenName = "TokenTest-" + randomUUID().toString();

        final var request = new CreateNeoTokenRequest();
        request.setToken(newToken(tokenName));
        request.setListed(false);
        request.setContractId("");

        Response response = client
                .target(apiRoot + "/blockchain/neo/token")
                .request()
                .header(authHeader, superUserClientContext.getSessionSecret())
                .post(Entity.entity(request, APPLICATION_JSON));

        assertEquals(response.getStatus(), 200);

        NeoToken neoToken = client
                .target(apiRoot + "/blockchain/neo/token/" + tokenName)
                .request()
                .header(authHeader, superUserClientContext.getSessionSecret())
                .get()
                .readEntity(NeoToken.class);

        assertNotNull(neoToken);
        assertNotNull(neoToken.getId());
        assertEquals(neoToken.getToken().getName(), tokenName);

        response = client
                .target(apiRoot + "/blockchain/neo/token/" + neoToken.getId())
                .request()
                .header(authHeader, superUserClientContext.getSessionSecret())
                .delete();

        assertEquals(response.getStatus(), 204);
    }

    @Test(dependsOnMethods = "createUser", dataProvider = "getAuthHeader")
    public void testUpdateToken(final String authHeader) {

        String tokenName = "TokenTest-" + randomUUID().toString();

        final var request = new CreateNeoTokenRequest();
        request.setToken(newToken(tokenName));
        request.setListed(false);
        request.setContractId("");

        NeoToken neoToken = client
                .target(apiRoot + "/blockchain/neo/token")
                .request()
                .header(authHeader, superUserClientContext.getSessionSecret())
                .post(Entity.entity(request, APPLICATION_JSON))
                .readEntity(NeoToken.class);

        assertNotNull(neoToken);
        assertNotNull(neoToken.getId());
        assertEquals(neoToken.getToken().getName(), tokenName);

        String updatedTokenName = "TokenTest-" + randomUUID().toString();
        UpdateNeoTokenRequest updateRequest = new UpdateNeoTokenRequest();
        neoToken.getToken().setName(updatedTokenName);
        updateRequest.setToken(neoToken.getToken());
        updateRequest.setListed(false);
        updateRequest.setContractId("");

        var updatedNeoToken = client
                .target(apiRoot + "/blockchain/neo/token/" + neoToken.getId())
                .request()
                .header(authHeader, superUserClientContext.getSessionSecret())
                .put(Entity.entity(updateRequest, APPLICATION_JSON))
                .readEntity(NeoToken.class);

        assertNotNull(updatedNeoToken);
        assertNotNull(updatedNeoToken.getId());
        assertEquals(updatedNeoToken.getToken().getName(), updatedTokenName);

        Response response = client
                .target(apiRoot + "/blockchain/neo/token/" + updatedNeoToken.getId())
                .request()
                .header(authHeader, superUserClientContext.getSessionSecret())
                .delete();

        assertEquals(response.getStatus(), 204);
    }

    private Token newToken(String tokenName) {
        final var token = new Token();
        token.setOwner("");
        token.setName(tokenName);
        token.setDescription("");
        List<String> tags = new ArrayList<>();
        tags.add("tag1");
        token.setTags(tags);
        token.setTotalSupply(1);
        token.setAccessOption("private");
        List<String> previewUrls = new ArrayList<>();
        previewUrls.add("");
        token.setPreviewUrls(previewUrls);
        List<String> assetUrls = new ArrayList<>();
        assetUrls.add("");
        token.setAssetUrls(assetUrls);
        Ownership ownership = new Ownership();
        StakeHolder stakeHolder = new StakeHolder();
        stakeHolder.setOwner("");
        stakeHolder.setVoting(false);
        stakeHolder.setShares(1000);
        List<StakeHolder> stakeHolders = new ArrayList<>();
        stakeHolders.add(stakeHolder);
        ownership.setStakeHolders(stakeHolders);
        ownership.setCapitalization(0);
        token.setOwnership(ownership);
        token.setTransferOptions("");
        return token;
    }
}
