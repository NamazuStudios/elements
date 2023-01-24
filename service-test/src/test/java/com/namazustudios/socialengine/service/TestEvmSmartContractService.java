package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.dao.mongo.UserTestFactory;
import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
import com.namazustudios.socialengine.model.blockchain.contract.CreateSmartContractRequest;
import com.namazustudios.socialengine.model.blockchain.contract.SmartContract;
import com.namazustudios.socialengine.model.blockchain.contract.SmartContractAddress;
import com.namazustudios.socialengine.model.blockchain.wallet.CreateVaultRequest;
import com.namazustudios.socialengine.model.blockchain.wallet.CreateWalletRequest;
import com.namazustudios.socialengine.model.blockchain.wallet.CreateWalletRequestAccount;
import com.namazustudios.socialengine.model.blockchain.wallet.Vault;
import com.namazustudios.socialengine.model.user.User;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.namazustudios.socialengine.model.crypto.PrivateKeyCrytpoAlgorithm.RSA_512;
import static com.namazustudios.socialengine.model.user.User.Level.SUPERUSER;
import static java.util.stream.Collectors.toMap;
import static org.testng.Assert.*;
import static org.testng.Assert.assertEquals;

public class TestEvmSmartContractService {

    private static final String CONTRACT_NAME = "evm_integration_test";

    public static final Map<BlockchainNetwork, String> CONTACT_ADDRESSES = Map.of(
            BlockchainNetwork.BSC_TEST, "0xEF5FAD1711BA258ff6a4AbB1d86D165100B87956"
    );

    public static final Map<BlockchainNetwork, String> WALLET_ADDRESSES = Map.of(
            BlockchainNetwork.BSC_TEST, "0x511663912ac4b4e55bdca865aebae6a58d2e5050"
    );

    public static final Map<BlockchainNetwork, String> WALLET_PRIVATE_KEYS = Map.of(
            BlockchainNetwork.BSC_TEST, "71e934f755e89effbb6436cdff469c7577261a366cab94e95f16f1d29da4e685"
    );

    private User user;

    private Vault vault;

    private SmartContract smartContract;

    private UserTestFactory userTestFactory;

    private EvmSmartContractService underTest;

    private VaultService vaultService;

    private WalletService walletService;

    private SmartContractService smartContractService;

    private Map<BlockchainNetwork, String> listingIds = new ConcurrentHashMap<>();

    @Factory
    public Object[] getTests() {
        return new Object[] {
                TestUtils.getInstance().getXodusTest(TestEvmSmartContractService.class),
                TestUtils.getInstance().getUnixFSTest(TestEvmSmartContractService.class)
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
        for (var network : CONTACT_ADDRESSES.keySet()) {

            final var address = WALLET_ADDRESSES.get(network);
            final var privateKey = WALLET_PRIVATE_KEYS.get(network);
            final var account = new CreateWalletRequestAccount();
            account.setAddress(address);
            account.setPrivateKey(privateKey);

            final var request = new CreateWalletRequest();
            request.setApi(network.api());
            request.setNetworks(new ArrayList<>(List.of(network)));
            request.setAccounts(new ArrayList<>(List.of(account)));
            request.setDisplayName("Integration Test Wallet");
            request.setPreferredAccount(0);

            getWalletService().createWallet(vault.getId(), request);

        }
    }

    @BeforeClass(dependsOnMethods = "setupVault")
    public void setupContract() {

        final var addresses = CONTACT_ADDRESSES
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

        smartContract = getSmartContractService().createSmartContract(request);

    }

    @DataProvider
    public Object[][] testNetworks() {
        return CONTACT_ADDRESSES
                .keySet()
                .stream()
                .map(n -> new Object[]{n})
                .toArray(Object[][]::new);
    }

    @Test(dataProvider = "testNetworks")
    public void testSend(final BlockchainNetwork blockchainNetwork) {

        final var name = "Test";
        final var desc = "This is a test";

        final var response = getUnderTest()
                .resolve(CONTRACT_NAME, blockchainNetwork)
                .open()
                .send(
                        "createListing",
                        List.of("string", "string"),
                        List.of(name, desc),
                        List.of("string", "uint256")
                );

        assertNotNull(response);

        final var logs = response.getLogs();
        assertEquals(logs.size(), 1);

        final var data = logs.get(0).getData();
        assertNotNull(data);

        final List<Object> decodedData = response.getDecodedLog();
        final var responseName = decodedData.get(0).toString();
        final var responseId = decodedData.get(1).toString();

        assertNotNull(responseId);
        assertEquals(decodedData.size(), 2);
        assertEquals(name, responseName);

        listingIds.put(blockchainNetwork, responseId);

    }

    @Test(dataProvider = "testNetworks", dependsOnMethods = "testSend")
    public void testCall(final BlockchainNetwork blockchainNetwork) {

        final var listingId = listingIds.get(blockchainNetwork);

        final var response = getUnderTest()
                .resolve(CONTRACT_NAME, blockchainNetwork)
                .open()
                .send(
                        "listings",
                        List.of("uint256"),
                        List.of(listingId),
                        List.of("string", "string", "uint256", "uint256")
                );

        assertTrue(response instanceof List);

        final var responseList = (List<?>) response;
        assertEquals(responseList.size(), 4);
        assertTrue(responseList.get(0) instanceof String);
        assertTrue(responseList.get(1) instanceof String);
        assertTrue(responseList.get(2) instanceof Integer);
        assertTrue(responseList.get(3) instanceof Integer);

    }

    public UserTestFactory getUserTestFactory() {
        return userTestFactory;
    }

    @Inject
    public void setUserTestFactory(UserTestFactory userTestFactory) {
        this.userTestFactory = userTestFactory;
    }

    public EvmSmartContractService getUnderTest() {
        return underTest;
    }

    @Inject
    public void setUnderTest(@Unscoped EvmSmartContractService underTest) {
        this.underTest = underTest;
    }

    public VaultService getVaultService() {
        return vaultService;
    }

    @Inject
    public void setVaultService(@Unscoped VaultService vaultService) {
        this.vaultService = vaultService;
    }

    public WalletService getWalletService() {
        return walletService;
    }

    @Inject
    public void setWalletService(@Unscoped WalletService walletService) {
        this.walletService = walletService;
    }

    public SmartContractService getSmartContractService() {
        return smartContractService;
    }

    @Inject
    public void setSmartContractService(@Unscoped SmartContractService smartContractService) {
        this.smartContractService = smartContractService;
    }

}
