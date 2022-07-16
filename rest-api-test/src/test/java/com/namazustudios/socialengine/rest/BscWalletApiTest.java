package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.model.ErrorResponse;
import com.namazustudios.socialengine.model.blockchain.bsc.CreateBscWalletRequest;
import com.namazustudios.socialengine.model.blockchain.bsc.BscWallet;
import com.namazustudios.socialengine.model.blockchain.bsc.UpdateBscWalletRequest;
import com.namazustudios.socialengine.model.savedata.CreateSaveDataDocumentRequest;
import com.namazustudios.socialengine.model.savedata.SaveDataDocument;
import com.namazustudios.socialengine.model.savedata.UpdateSaveDataDocumentRequest;
import com.namazustudios.socialengine.rest.model.SaveDataDocumentPagination;
import com.namazustudios.socialengine.rt.util.Hex;
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

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import static com.namazustudios.socialengine.Headers.SESSION_SECRET;
import static com.namazustudios.socialengine.Headers.SOCIALENGINE_SESSION_SECRET;
import static com.namazustudios.socialengine.exception.ErrorCode.*;
import static com.namazustudios.socialengine.exception.ErrorCode.NOT_FOUND;
import static com.namazustudios.socialengine.rest.TestUtils.TEST_API_ROOT;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static java.util.stream.IntStream.range;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.testng.Assert.*;
import static org.testng.AssertJUnit.assertEquals;

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
    private ClientContext user0;

    @Inject
    private ClientContext user1;

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

    @Test(dataProvider = "getClientContextsForOpposingUsers")
    public void testCreateUserSaveBscWalletFailsAcrossUsers(final ClientContext context,
                                                           final ClientContext other) {

        String walletName = "WalletTest-" + randomUUID().toString();

        final var request = new CreateBscWalletRequest();
        request.setUserId(other.getUser().getId());
        request.setDisplayName(walletName);


        final var response = client
                .target(apiRoot + "/blockchain/bsc/wallet")
                .request()
                .header("Authorization", format("Bearer %s", context.getSessionSecret()))
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        final int status = response.getStatus();
        assertEquals(403, status);

        final var error = response.readEntity(ErrorResponse.class);
        assertEquals(FORBIDDEN.toString(), error.getCode());

    }

    @Test(dataProvider = "getClientContexts")
    public void testCheckedUpdateUserSaveBscWallet(final ClientContext context) {
        String walletName = "WalletTest-" + randomUUID().toString();

        final var request = new CreateBscWalletRequest();
        request.setUserId(context.getUser().getId());
        request.setDisplayName(walletName);

        var bscWalletResponse = client
                .target(apiRoot + "/blockchain/bsc/wallet")
                .request()
                .header("Authorization", format("Bearer %s", context.getSessionSecret()))
                .post(Entity.entity(request, APPLICATION_JSON));

        var bscWallet = bscWalletResponse.readEntity(BscWallet.class);

        assertNotNull(bscWallet);
        assertNotNull(bscWallet.getId());
        assertEquals(bscWallet.getUser().getId(), context.getUser().getId());
        assertEquals(bscWallet.getDisplayName(), walletName);

        String updatedWalletName = "WalletTest-" + randomUUID().toString();
        UpdateBscWalletRequest updateRequest = new UpdateBscWalletRequest();
        updateRequest.setDisplayName(updatedWalletName);

        var updatedBscWallet = client
                .target(apiRoot + "/blockchain/bsc/wallet/" + bscWallet.getId())
                .request()
                .header("Authorization", format("Bearer %s", context.getSessionSecret()))
                .put(Entity.entity(updateRequest, APPLICATION_JSON))
                .readEntity(BscWallet.class);

        assertNotNull(updatedBscWallet);
        assertNotNull(updatedBscWallet.getId());
        assertEquals(updatedBscWallet.getUser().getId(), context.getUser().getId());
        assertNotEquals(updatedBscWallet.getDisplayName(), walletName);
        assertEquals(updatedBscWallet.getDisplayName(), updatedWalletName);
        assertEquals(context.getUser().getId(), updatedBscWallet.getUser().getId());

        Response response = client
                .target(apiRoot + "/blockchain/bsc/wallet/" + bscWallet.getId())
                .request()
                .header("Authorization", format("Bearer %s", context.getSessionSecret()))
                .delete();

        assertEquals(response.getStatus(), 204);

    }
}
