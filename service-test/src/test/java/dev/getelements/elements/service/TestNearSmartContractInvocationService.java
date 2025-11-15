package dev.getelements.elements.service;

import com.syntifi.near.api.common.exception.NearException;
import dev.getelements.elements.dao.mongo.test.UserTestFactory;
import dev.getelements.elements.sdk.model.blockchain.BlockchainNetwork;
import dev.getelements.elements.sdk.model.blockchain.contract.CreateSmartContractRequest;
import dev.getelements.elements.sdk.model.blockchain.contract.SmartContractAddress;
import dev.getelements.elements.sdk.model.blockchain.contract.near.NearContractFunctionCallResult;
import dev.getelements.elements.sdk.model.blockchain.wallet.CreateVaultRequest;
import dev.getelements.elements.sdk.model.blockchain.wallet.CreateWalletRequest;
import dev.getelements.elements.sdk.model.blockchain.wallet.CreateWalletRequestAccount;
import dev.getelements.elements.sdk.model.blockchain.wallet.Vault;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.blockchain.NearSmartContractInvocationService;
import dev.getelements.elements.sdk.service.blockchain.SmartContractService;
import dev.getelements.elements.sdk.service.blockchain.VaultService;
import dev.getelements.elements.sdk.service.blockchain.WalletService;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static dev.getelements.elements.sdk.model.crypto.PrivateKeyCrytpoAlgorithm.RSA_512;
import static dev.getelements.elements.sdk.model.user.User.Level.SUPERUSER;
import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;
import static java.util.stream.Collectors.toMap;
import static org.testng.Assert.assertNotNull;

public class TestNearSmartContractInvocationService {

    private static final Logger logger = LoggerFactory.getLogger(TestNearSmartContractInvocationService.class);

    private static final String CONTRACT_NAME = "near_integration_test";

    private static final BlockchainNetwork testNet = BlockchainNetwork.NEAR_TEST;
    public static final Map<BlockchainNetwork, String> CONTRACT_ADDRESSES = Map.of(
            testNet, "guest-book.testnet"
    );

    public static final Map<String, String> WALLET_ADDRESSES = Map.of(
            "alice", "d1fe5b15a491ad5bf4c1cdb290f17bee71d21449ddac80fb7b92f38fe530cf5e",
            "bob", "ba56c617a2e8dd1ba484c48d40c659f088af74bd1bd8dc3c37ef1c2280547f61"
    );

    public static final Map<String, String> WALLET_PUBLIC_KEYS = Map.of(
            "alice", "ed25519:F8jARHGZdHqnwrxrnv1pFVzzirXZR2vJzeYbvwQbxZyP",
            "bob", "ed25519:DYPaMCfE8xQUDQ19NnGCr18WQiqYwjSoaeXpKTDFv3kt"
    );

    public static final Map<String, String> WALLET_PRIVATE_KEYS = Map.of(
            "alice", "ed25519:32UfEkBGTFpfu6M7RebN1JqMDrdf1YyztgYmcUG5XcRkEraJioFZLPtBvYVmAVvnjWAToSsWScJYSFViv8MaATRF",
            "bob", "ed25519:59drpA65TXWoyYuQWuRgKcrxCcQaCrZB9YiUbVBjMrmch6utfoyVvDZ8Liz5mrhosr7szwrmDvVTZ7jrFbGCCaR6"
    );

    private User user;

    private Vault vault;

    private UserTestFactory userTestFactory;

    private NearSmartContractInvocationService underTest;

    private VaultService vaultService;

    private WalletService walletService;

    private SmartContractService smartContractService;

    @Factory
    public Object[] getTests() {
        return new Object[] {
                TestUtils.getInstance().getUnixFSTest(TestNearSmartContractInvocationService.class)
        };
    }

    @BeforeClass
    public void setupUser() {
        user = getUserTestFactory().createTestUser(u -> u.setLevel(SUPERUSER));
    }

    @BeforeClass(dependsOnMethods = "setupUser")
    public void setupVault() {
        final var request = new CreateVaultRequest();
        request.setAlgorithm(RSA_512);
        request.setDisplayName("Integration Test Vault");
        request.setUserId(user.getId());
        vault = getVaultService().createVault(request);
    }

    @BeforeClass(dependsOnMethods = "setupVault")
    public void setupWallets() {

        final var name = "alice";
        final var address = WALLET_PUBLIC_KEYS.get(name);
        final var privateKey = WALLET_PRIVATE_KEYS.get(name);
        final var account = new CreateWalletRequestAccount();
        account.setAddress(address);
        account.setPrivateKey(privateKey);

        final var request = new CreateWalletRequest();
        request.setApi(BlockchainNetwork.NEAR_TEST.api());
        request.setNetworks(new ArrayList<>(List.of(testNet)));
        request.setAccounts(new ArrayList<>(List.of(account)));
        request.setDisplayName(name);
        request.setPreferredAccount(0);

        getWalletService().createWallet(vault.getId(), request);

    }

    @BeforeClass(dependsOnMethods = "setupVault")
    public void setupContract() {

        final var addresses = CONTRACT_ADDRESSES
                .entrySet()
                .stream()
                .collect(toMap(Map.Entry::getKey, e -> {
                    final var address = new SmartContractAddress();
                    address.setAddress(e.getValue());
                    return address;
                }));

        final var request = new CreateSmartContractRequest();
        request.setDisplayName("Integration Test Contract");
        request.setName(CONTRACT_NAME);
        request.setVaultId(vault.getId());
        request.setAddresses(addresses);

        getSmartContractService().createSmartContract(request);

    }

    @DataProvider
    public Object[][] testNetworks() {
        return CONTRACT_ADDRESSES
                .keySet()
                .stream()
                .map(n -> new Object[]{n})
                .toArray(Object[][]::new);
    }

    @Test(dataProvider = "testNetworks", expectedExceptions = NearException.class, enabled = false)
    public void testSend(final BlockchainNetwork blockchainNetwork) {

        final var recipientId = WALLET_ADDRESSES.get("bob");

        final List<Map.Entry<String, Map<String, Object>>> actions = List.of(
                Map.entry("transfer", Map.of( "deposit", "1"))
        );

        final var response = getUnderTest()
                .resolve(CONTRACT_NAME, blockchainNetwork)
                .open()
                .sendDirect(recipientId, actions);

        assertNotNull(response);
    }

    @Test(dataProvider = "testNetworks", dependsOnMethods = "testSend", enabled = false)
    public void testCall(final BlockchainNetwork blockchainNetwork) {

        final var contractAddress = CONTRACT_ADDRESSES.get(blockchainNetwork);
        final var methodName = "getMessages";
        final Map<String, ?> params = Map.of();

        final NearContractFunctionCallResult response = getUnderTest()
                .resolve(CONTRACT_NAME, blockchainNetwork)
                .open()
                .call(contractAddress, methodName, params);

        logger.info("Result: " + new String(response.getResult()) + "\n" +
                "Logs: " + response.getLogs().toString() + "\n" +
                "Error: " + response.getError());

        assertNotNull(response);
    }

    public UserTestFactory getUserTestFactory() {
        return userTestFactory;
    }

    @Inject
    public void setUserTestFactory(UserTestFactory userTestFactory) {
        this.userTestFactory = userTestFactory;
    }

    public NearSmartContractInvocationService getUnderTest() {
        return underTest;
    }

    @Inject
    public void setUnderTest(@Named(UNSCOPED) NearSmartContractInvocationService underTest) {
        this.underTest = underTest;
    }

    public VaultService getVaultService() {
        return vaultService;
    }

    @Inject
    public void setVaultService(@Named(UNSCOPED) VaultService vaultService) {
        this.vaultService = vaultService;
    }

    public WalletService getWalletService() {
        return walletService;
    }

    @Inject
    public void setWalletService(@Named(UNSCOPED) WalletService walletService) {
        this.walletService = walletService;
    }

    public SmartContractService getSmartContractService() {
        return smartContractService;
    }

    @Inject
    public void setSmartContractService(@Named(UNSCOPED) SmartContractService smartContractService) {
        this.smartContractService = smartContractService;
    }
}
