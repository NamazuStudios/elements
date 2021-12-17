package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.model.blockchain.CreateNeoWalletRequest;
import com.namazustudios.socialengine.model.blockchain.NeoWallet;
import com.namazustudios.socialengine.model.blockchain.UpdateNeoWalletRequest;
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

import java.util.HashMap;

import static com.namazustudios.socialengine.Headers.SESSION_SECRET;
import static com.namazustudios.socialengine.Headers.SOCIALENGINE_SESSION_SECRET;
import static com.namazustudios.socialengine.rest.TestUtils.*;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.testng.Assert.*;

public class NeoWalletApiTest {

    @Factory
    public Object[] getTests() {
        return new Object[] {
                TestUtils.getInstance().getXodusTest(NeoWalletApiTest.class),
                TestUtils.getInstance().getUnixFSTest(NeoWalletApiTest.class)
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
                .createSuperuser("walletAdmin")
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
    public void testCreateAndDeleteWallet(final String authHeader) {

        String walletName = "WalletTest-" + randomUUID().toString();

        final var request = new CreateNeoWalletRequest();
        request.setUserId(user.getId());
        request.setDisplayName(walletName);

        NeoWallet neoWallet = client
                .target(apiRoot + "/blockchain/neo/wallet")
                .request()
                .header(authHeader, superUserClientContext.getSessionSecret())
                .post(Entity.entity(request, APPLICATION_JSON))
                .readEntity(NeoWallet.class);

        assertNotNull(neoWallet);
        assertNotNull(neoWallet.getId());
        assertEquals(neoWallet.getUser().getId(), user.getId());
        assertEquals(neoWallet.getDisplayName(), walletName);

        String req = "/blockchain/neo/wallet/" + neoWallet.getId();

        Response response = client
                .target(apiRoot + req)
                .request()
                .header(authHeader, superUserClientContext.getSessionSecret())
                .delete();

        assertEquals(response.getStatus(), 204);
    }

    @Test(dependsOnMethods = "createUser", dataProvider = "getAuthHeader")
    public void testGetWallet(final String authHeader) {

        String walletName = "WalletTest-" + randomUUID().toString();

        final var request = new CreateNeoWalletRequest();
        request.setUserId(user.getId());
        request.setDisplayName(walletName);

        Response response = client
                .target(apiRoot + "/blockchain/neo/wallet")
                .request()
                .header(authHeader, superUserClientContext.getSessionSecret())
                .post(Entity.entity(request, APPLICATION_JSON));

        assertEquals(response.getStatus(), 200);

        var neoWallet = client
                .target(apiRoot + "/blockchain/neo/wallet/" + walletName)
                .request()
                .header(authHeader, superUserClientContext.getSessionSecret())
                .get()
                .readEntity(NeoWallet.class);

        assertNotNull(neoWallet);
        assertNotNull(neoWallet.getId());
        assertEquals(neoWallet.getUser().getId(), user.getId());
        assertEquals(neoWallet.getDisplayName(), walletName);

        response = client
                .target(apiRoot + "/blockchain/neo/wallet/" + neoWallet.getId())
                .request()
                .header(authHeader, superUserClientContext.getSessionSecret())
                .delete();

        assertEquals(response.getStatus(), 204);
    }

    @Test(dependsOnMethods = "createUser", dataProvider = "getAuthHeader")
    public void testUpdateWallet(final String authHeader) {

        String walletName = "WalletTest-" + randomUUID().toString();

        final var request = new CreateNeoWalletRequest();
        request.setUserId(user.getId());
        request.setDisplayName(walletName);

        NeoWallet neoWallet = client
                .target(apiRoot + "/blockchain/neo/wallet")
                .request()
                .header(authHeader, superUserClientContext.getSessionSecret())
                .post(Entity.entity(request, APPLICATION_JSON))
                .readEntity(NeoWallet.class);

        assertNotNull(neoWallet);
        assertNotNull(neoWallet.getId());
        assertEquals(neoWallet.getUser().getId(), user.getId());
        assertEquals(neoWallet.getDisplayName(), walletName);

        String updatedWalletName = "WalletTest-" + randomUUID().toString();
        UpdateNeoWalletRequest updateRequest = new UpdateNeoWalletRequest();
        updateRequest.setDisplayName(updatedWalletName);

        var updatedNeoWallet = client
                .target(apiRoot + "/blockchain/neo/wallet/" + neoWallet.getId())
                .request()
                .header(authHeader, superUserClientContext.getSessionSecret())
                .put(Entity.entity(updateRequest, APPLICATION_JSON))
                .readEntity(NeoWallet.class);

        assertNotNull(updatedNeoWallet);
        assertNotNull(updatedNeoWallet.getId());
        assertEquals(updatedNeoWallet.getUser().getId(), user.getId());
        assertNotEquals(updatedNeoWallet.getDisplayName(), walletName);
        assertEquals(updatedNeoWallet.getDisplayName(), updatedWalletName);

        Response response = client
                .target(apiRoot + "/blockchain/neo/wallet/" + neoWallet.getId())
                .request()
                .header(authHeader, superUserClientContext.getSessionSecret())
                .delete();

        assertEquals(response.getStatus(), 204);
    }
}
