package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.BlockchainConstants.MintStatus;
import com.namazustudios.socialengine.dao.NeoTokenDao;
import com.namazustudios.socialengine.model.blockchain.Ownership;
import com.namazustudios.socialengine.model.blockchain.StakeHolder;
import com.namazustudios.socialengine.model.blockchain.Token;
import com.namazustudios.socialengine.model.blockchain.neo.CreateNeoTokenRequest;
import com.namazustudios.socialengine.model.blockchain.neo.NeoToken;
import com.namazustudios.socialengine.model.blockchain.neo.UpdateNeoTokenRequest;
import com.namazustudios.socialengine.rest.model.NeoTokenPagination;
import com.namazustudios.socialengine.util.PaginationWalker;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static com.namazustudios.socialengine.Headers.SESSION_SECRET;
import static com.namazustudios.socialengine.Headers.SOCIALENGINE_SESSION_SECRET;
import static com.namazustudios.socialengine.rest.TestUtils.TEST_API_ROOT;
import static java.lang.String.format;
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

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private Client client;

    @Inject
    private ClientContext superUserClientContext;

    @Inject
    private NeoTokenDao neoTokenDao;

    @DataProvider
    public static Object[][] getMintStatus() {
        return Stream
            .of(MintStatus.values())
            .map(s -> new Object[] {s})
            .toArray(Object[][]::new);
    }

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
            .createSuperuser("tokenAdmin")
            .createSession();
    }

    @BeforeClass
    public void createTestTokens() {
        for (var status : MintStatus.values()) {
            for (int i = 0 ; i < 10; ++ i) {

                var token = newToken();
                var request = new CreateNeoTokenRequest();
                request.setContractId("");
                request.setListed(true);
                request.setToken(token);

                final var created = neoTokenDao.createToken(request);
                neoTokenDao.setMintStatusForToken(created.getId(), status);

            }

        }
    }

    @Test(dataProvider = "getAuthHeader")
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

    @Test(dataProvider = "getAuthHeader")
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

        var created = response.readEntity(NeoToken.class);

        assertEquals(response.getStatus(), 200);

        var neoToken = client
            .target(apiRoot + "/blockchain/neo/token/" + created.getId())
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

    @Test(dataProvider = "getAuthHeader")
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

        var response = client
            .target(apiRoot + "/blockchain/neo/token/" + updatedNeoToken.getId())
            .request()
            .header(authHeader, superUserClientContext.getSessionSecret())
            .delete();

        assertEquals(response.getStatus(), 204);

    }

    @Test
    public void testGetTokens() {

        final var called = new AtomicBoolean();

        final PaginationWalker.WalkFunction<NeoToken> walkFunction = (offset, count) -> {
            final var response = client.target(format("%s/blockchain/neo/token?offset=%d&count=%d",
                    apiRoot,
                    offset,
                    count)
            )
            .request()
            .header("Authorization", format("Bearer %s", superUserClientContext.getSessionSecret()))
            .get(NeoTokenPagination.class);
            called.set(true);
            return response;
        };

        new PaginationWalker().forEach(walkFunction, neoToken -> {});
        assertTrue(called.get());

    }

    @Test(dataProvider = "getMintStatus")
    public void testGetTokensFilterByStatus(final MintStatus mintStatus) {

        final var called = new AtomicBoolean();

        final PaginationWalker.WalkFunction<NeoToken> walkFunction = (offset, count) -> {
            final var response = client.target(format("%s/blockchain/neo/token?offset=%d&count=%d&mintStatus=%s",
                    apiRoot,
                    offset,
                    count,
                    mintStatus.toString())
                )
                .request()
                .header("Authorization", format("Bearer %s", superUserClientContext.getSessionSecret()))
                .get(NeoTokenPagination.class);
            called.set(true);
            return response;
        };

        new PaginationWalker().forEach(walkFunction, i -> assertEquals(i.getMintStatus(), mintStatus));
        assertTrue(called.get());

    }

    private Token newToken() {
        final var tokenName = "TokenTest-" + randomUUID().toString();
        return newToken(tokenName);
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
