package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.model.blockchain.bsc.CreateBscWalletRequest;
import com.namazustudios.socialengine.model.blockchain.bsc.BscWallet;
import com.namazustudios.socialengine.model.blockchain.bsc.UpdateBscWalletRequest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static com.namazustudios.socialengine.Headers.SESSION_SECRET;
import static com.namazustudios.socialengine.Headers.SOCIALENGINE_SESSION_SECRET;
import static com.namazustudios.socialengine.rest.TestUtils.TEST_API_ROOT;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.testng.Assert.*;

public class BscWalletApiTest {

    @Factory
    public Object[] getTests() {
        return new Object[] {
                TestUtils.getInstance().getXodusTest(BscWalletApiTest.class),
                TestUtils.getInstance().getUnixFSTest(BscWalletApiTest.class)
        };
    }

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

    @Test(dependsOnMethods = "createUser", dataProvider = "getAuthHeader")
    public void testCreateAndDeleteWallet(final String authHeader) {

        String walletName = "WalletTest-" + randomUUID().toString();

        final var request = new CreateBscWalletRequest();
        request.setUserId(superUserClientContext.getUser().getId());
        request.setDisplayName(walletName);

        BscWallet bscWallet = client
                .target(apiRoot + "/blockchain/bsc/wallet")
                .request()
                .header(authHeader, superUserClientContext.getSessionSecret())
                .post(Entity.entity(request, APPLICATION_JSON))
                .readEntity(BscWallet.class);

        assertNotNull(bscWallet);
        assertNotNull(bscWallet.getId());
        assertEquals(bscWallet.getUser().getId(), superUserClientContext.getUser().getId());
        assertEquals(bscWallet.getDisplayName(), walletName);

        String req = "/blockchain/bsc/wallet/" + bscWallet.getId();

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

        final var request = new CreateBscWalletRequest();
        request.setUserId(superUserClientContext.getUser().getId());
        request.setDisplayName(walletName);

        Response response = client
                .target(apiRoot + "/blockchain/bsc/wallet")
                .request()
                .header(authHeader, superUserClientContext.getSessionSecret())
                .post(Entity.entity(request, APPLICATION_JSON));

        assertEquals(response.getStatus(), 200);

        var bscWallet = client
                .target(apiRoot + "/blockchain/bsc/wallet/" + walletName)
                .request()
                .header(authHeader, superUserClientContext.getSessionSecret())
                .get()
                .readEntity(BscWallet.class);

        assertNotNull(bscWallet);
        assertNotNull(bscWallet.getId());
        assertEquals(bscWallet.getUser().getId(), superUserClientContext.getUser().getId());
        assertEquals(bscWallet.getDisplayName(), walletName);

        response = client
                .target(apiRoot + "/blockchain/bsc/wallet/" + bscWallet.getId())
                .request()
                .header(authHeader, superUserClientContext.getSessionSecret())
                .delete();

        assertEquals(response.getStatus(), 204);
    }

    @Test(dependsOnMethods = "createUser", dataProvider = "getAuthHeader")
    public void testUpdateWallet(final String authHeader) {

        String walletName = "WalletTest-" + randomUUID().toString();

        final var request = new CreateBscWalletRequest();
        request.setUserId(superUserClientContext.getUser().getId());
        request.setDisplayName(walletName);

        BscWallet bscWallet = client
                .target(apiRoot + "/blockchain/bsc/wallet")
                .request()
                .header(authHeader, superUserClientContext.getSessionSecret())
                .post(Entity.entity(request, APPLICATION_JSON))
                .readEntity(BscWallet.class);

        assertNotNull(bscWallet);
        assertNotNull(bscWallet.getId());
        assertEquals(bscWallet.getUser().getId(), superUserClientContext.getUser().getId());
        assertEquals(bscWallet.getDisplayName(), walletName);

        String updatedWalletName = "WalletTest-" + randomUUID().toString();
        UpdateBscWalletRequest updateRequest = new UpdateBscWalletRequest();
        updateRequest.setDisplayName(updatedWalletName);

        var updatedBscWallet = client
                .target(apiRoot + "/blockchain/bsc/wallet/" + bscWallet.getId())
                .request()
                .header(authHeader, superUserClientContext.getSessionSecret())
                .put(Entity.entity(updateRequest, APPLICATION_JSON))
                .readEntity(BscWallet.class);

        assertNotNull(updatedBscWallet);
        assertNotNull(updatedBscWallet.getId());
        assertEquals(updatedBscWallet.getUser().getId(), superUserClientContext.getUser().getId());
        assertNotEquals(updatedBscWallet.getDisplayName(), walletName);
        assertEquals(updatedBscWallet.getDisplayName(), updatedWalletName);

        Response response = client
                .target(apiRoot + "/blockchain/bsc/wallet/" + bscWallet.getId())
                .request()
                .header(authHeader, superUserClientContext.getSessionSecret())
                .delete();

        assertEquals(response.getStatus(), 204);
    }
}
