package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.BlockchainConstants;
import com.namazustudios.socialengine.dao.BscSmartContractDao;
import com.namazustudios.socialengine.dao.BscWalletDao;
import com.namazustudios.socialengine.model.blockchain.contract.EVMInvokeContractRequest;
import com.namazustudios.socialengine.model.blockchain.contract.EVMInvokeContractResponse;
import com.namazustudios.socialengine.model.blockchain.PatchSmartContractRequest;
import com.namazustudios.socialengine.model.blockchain.bsc.BscWallet;
import com.namazustudios.socialengine.service.blockchain.bsc.Bscw3jClient;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.web3j.crypto.Credentials;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import java.math.BigInteger;
import java.util.List;

import static com.namazustudios.socialengine.Headers.SESSION_SECRET;
import static com.namazustudios.socialengine.rest.TestUtils.TEST_API_ROOT;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.testng.Assert.*;


//TODO: This should really be using a temporary BSC blockchain container to test against,
// but for now we will use testnet with a hardcoded contract address and credentials
public class BscContractApiTest {

    @Factory
    public Object[] getTests() {
        return new Object[] {
                TestUtils.getInstance().getXodusTest(BscContractApiTest.class),
                TestUtils.getInstance().getUnixFSTest(BscContractApiTest.class)
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
    private BscWalletDao bscWalletDao;

    @Inject
    private BscSmartContractDao bscContractDao;

    @Inject
    private Bscw3jClient bscw3JClient;

    private static final String BSC_TESTNET_URL = "https://data-seed-prebsc-1-s1.binance.org:8545";
    private static final BigInteger GAS_PRICE = BigInteger.valueOf(20000000000L);
    private static final BigInteger GAS_LIMIT = BigInteger.valueOf(6721975);
    private static final String WALLET_PASSWORD = "secr3t";

    private String contractId;
    private String listingId = "0";

    @DataProvider
    public Object[][] getAuthHeader() {
        return new Object[][] {
                new Object[] { SESSION_SECRET },
//                new Object[] { SOCIALENGINE_SESSION_SECRET }
        };
    }

    @BeforeClass
    public void createUser() {
        superUserClientContext
                .createSuperuser("tokenAdmin")
                .createSession();
    }

    @BeforeClass
    public void importWallet() {

        final var displayName = "Test Admin Wallet";
        final var privateKey = "0xc9aa92ff79ca085f7cc421227fe9f418d76933aadf0f81b595acd4722d63c943";
        final var w3jWallet = bscw3JClient.createWallet(displayName, WALLET_PASSWORD, privateKey);
        final var bscWallet = new BscWallet();

        bscWallet.setDisplayName(displayName);
        bscWallet.setWallet(w3jWallet);
        bscWallet.setUser(superUserClientContext.getUser());

        bscWalletDao.createWallet(bscWallet);
    }

    @BeforeClass
    public void registerContract() {
        final var contractAddress = "0xEF5FAD1711BA258ff6a4AbB1d86D165100B87956";
        final var bscContractRequest = new PatchSmartContractRequest();
        final var adminWalletPagination =
                bscWalletDao.getWallets(0, 20, superUserClientContext.getUser().getId());
        final var wallet = adminWalletPagination.getObjects().stream().findFirst().get();

        bscContractRequest.setBlockchain(BlockchainConstants.Names.BSC);
        bscContractRequest.setDisplayName("R2D Testnet Deals Contract");
        bscContractRequest.setScriptHash(contractAddress);
        bscContractRequest.setWalletId(wallet.getId());

        final var contract = bscContractDao.patchBscSmartContract(bscContractRequest);

        contractId = contract.getId();
    }

    //TODO: This test currently requires a GAS fee, so it should be left disabled for CI
    @Test(dataProvider = "getAuthHeader", enabled = false)
    public void testSend(final String authHeader) {

        final var name = "Test";
        final var desc = "This is a test";
        final var request = new EVMInvokeContractRequest();
        request.setContractId(contractId);
        request.setPassword(WALLET_PASSWORD);
        request.setMethodName("createListing");
        request.setInputTypes(List.of("string", "string"));
        request.setParameters(List.of(name, desc));
        request.setOutputTypes(List.of("string", "uint256"));

        final var response = client
                .target(apiRoot + "/blockchain/bsc/contract/send")
                .request()
                .header(authHeader, superUserClientContext.getSessionSecret())
                .post(Entity.entity(request, APPLICATION_JSON))
                .readEntity(EVMInvokeContractResponse.class);

        assertNotNull(response);

        final var logs = response.getLogs();
        assertTrue(logs.size() == 1);

        final var data = logs.get(0).getData();
        assertNotNull(data);

        final List<Object> decodedData = response.getDecodedLog();
        final var responseName = decodedData.get(0).toString();
        final var responseId = decodedData.get(1).toString();

        assertEquals(decodedData.size(), 2);
        assertEquals(name, responseName);

        listingId = responseId;
    }

    //TODO: This test currently requires a GAS fee, so it should be left disabled and run manually as needed.
    @Test(dataProvider = "getAuthHeader", dependsOnMethods = "testSend", enabled = false)
    public void testCall(final String authHeader) {

        final var request = new EVMInvokeContractRequest();
        request.setContractId(contractId);
        request.setPassword(WALLET_PASSWORD);
        request.setMethodName("listings");
        request.setInputTypes(List.of("uint256"));
        request.setParameters(List.of(listingId));
        request.setOutputTypes(List.of("string", "string", "uint256", "uint256"));

        final var response = client
                .target(apiRoot + "/blockchain/bsc/contract/call")
                .request()
                .header(authHeader, superUserClientContext.getSessionSecret())
                .post(Entity.entity(request, APPLICATION_JSON))
                .readEntity(List.class);

        assertNotNull(response);

        //TODO: This should change when we create a more generic contract for testing
        assertTrue(response.size() == 4);
        assertTrue(response.get(0) instanceof String);
        assertTrue(response.get(1) instanceof String);
        assertTrue(response.get(2) instanceof Integer);
        assertTrue(response.get(3) instanceof Integer);

    }


    private static Credentials getCredentials() {
        return Credentials.create("0xc9aa92ff79ca085f7cc421227fe9f418d76933aadf0f81b595acd4722d63c943");
    }

    public static void main(String[] args) {
        final var credentials = getCredentials();
        System.out.println(credentials.getAddress());
    }

}
