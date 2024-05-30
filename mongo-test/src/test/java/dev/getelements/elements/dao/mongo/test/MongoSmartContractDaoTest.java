package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.dao.SmartContractDao;
import dev.getelements.elements.dao.VaultDao;
import dev.getelements.elements.dao.WalletDao;
import dev.getelements.elements.exception.blockchain.SmartContractNotFoundException;
import dev.getelements.elements.model.blockchain.BlockchainApi;
import dev.getelements.elements.model.blockchain.BlockchainNetwork;
import dev.getelements.elements.model.blockchain.contract.SmartContract;
import dev.getelements.elements.model.blockchain.contract.SmartContractAddress;
import dev.getelements.elements.model.blockchain.wallet.Vault;
import dev.getelements.elements.model.blockchain.wallet.VaultKey;
import dev.getelements.elements.model.blockchain.wallet.Wallet;
import dev.getelements.elements.model.blockchain.wallet.WalletAccount;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.util.PaginationWalker;
import org.bson.types.ObjectId;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static dev.getelements.elements.dao.mongo.test.MongoWalletDaoTest.randomKey;
import static dev.getelements.elements.model.crypto.PrivateKeyCrytpoAlgorithm.RSA_512;
import static dev.getelements.elements.model.user.User.Level.SUPERUSER;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.testng.Assert.*;

@Guice(modules = IntegrationTestModule.class)
public class MongoSmartContractDaoTest {

    public static final String SUBJECT_TEST_NAME_BASE = "integration_test_dao_contract";

    private SmartContractDao underTest;

    private VaultDao vaultDao;

    private WalletDao walletDao;

    private UserTestFactory userTestFactory;

    private User adminUser;

    private Vault adminVault;

    private List<Wallet> wallets;

    private final Map<String, SmartContract> smartContracts = new ConcurrentHashMap<>();

    @DataProvider
    public Object[][] testContractNames() {
        return IntStream.range(0, 50)
                .mapToObj(i -> format("%s_%s", SUBJECT_TEST_NAME_BASE, i))
                .map(name -> new Object[]{name})
                .toArray(Object[][]::new);
    }

    @BeforeClass
    public void createTestUsersAndWallet() {

        adminUser = getUserTestFactory().createTestUser(u -> u.setLevel(SUPERUSER));

        final var key = new VaultKey();
        key.setEncrypted(false);
        key.setAlgorithm(RSA_512);
        key.setPublicKey(randomKey());
        key.setPrivateKey(randomKey());

        final var vault = new Vault();
        vault.setKey(key);
        vault.setUser(adminUser);
        vault.setDisplayName("Admin Vault");

        adminVault = getVaultDao().createVault(vault);

        wallets = Stream.of(BlockchainApi.values())
                .map(api -> {

                    final var identity = new WalletAccount();
                    identity.setEncrypted(false);
                    identity.setAddress(randomKey());
                    identity.setPrivateKey(randomKey());

                    final var wallet = new Wallet();
                    wallet.setApi(api);
                    wallet.setUser(adminUser);
                    wallet.setVault(adminVault);
                    wallet.setNetworks(api.networks().collect(toList()));
                    wallet.setDisplayName("Test Wallet: " + api);
                    wallet.setPreferredAccount(0);
                    wallet.setAccounts(new ArrayList<>(List.of(identity)));

                    return getWalletDao().createWallet(wallet);

                }).collect(toList());

    }

    @DataProvider
    public Object[][] blockchainNetworks() {
        return Stream
                .of(BlockchainNetwork.values())
                .map(protocol -> new Object[]{protocol})
                .toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] blockchainApi() {
        return Stream
                .of(BlockchainApi.values())
                .map(protocol -> new Object[]{protocol})
                .toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] wallets() {
        return wallets
                .stream()
                .map(u -> new Object[]{u})
                .toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] smartContracts() {
        return smartContracts
                .values()
                .stream()
                .map(c -> new Object[]{c})
                .toArray(Object[][]::new);
    }

    @Test(dataProvider = "testContractNames", groups = "create")
    public void testCreateSmartContract(final String testSubjectName) {

        final var addresses = Stream
                .of(BlockchainNetwork.values())
                .collect(toMap(network -> network, network -> {
                    final var address = new SmartContractAddress();
                    address.setAddress(randomKey());
                    return address;
                }));

        final var metadata = Stream
                .of(BlockchainNetwork.values())
                .collect(toMap(BlockchainNetwork::toString, network -> (Object) "Supported"));

        final var contract = new SmartContract();
        contract.setName(testSubjectName);
        contract.setMetadata(metadata);
        contract.setAddresses(addresses);
        contract.setDisplayName("Test Contract.");
        contract.setVault(adminVault);

        final var created = getUnderTest().createSmartContract(contract);
        assertNotNull(created.getId());
        assertEquals(created.getVault(), adminVault);
        assertEquals(created.getDisplayName(), contract.getDisplayName());
        assertEquals(created.getAddresses(), contract.getAddresses());

        smartContracts.put(created.getId(), created);

    }

    @Test(dataProvider = "smartContracts", groups = "update", dependsOnGroups = "create")
    public void testUpdateSmartContract(final SmartContract smartContract) {

        final var addresses = new HashMap<BlockchainNetwork, SmartContractAddress>();

        smartContract.getAddresses().forEach((network, address) -> {
            final var newAddress = new SmartContractAddress();
            newAddress.setAddress(randomKey());
            addresses.put(network, address);
        });

        final var metadata = new HashMap<String, Object>();
        metadata.putAll(smartContract.getMetadata());

        final var update = new SmartContract();
        update.setName(smartContract.getName());
        update.setId(smartContract.getId());
        update.setDisplayName(smartContract.getDisplayName());
        update.setMetadata(smartContract.getMetadata());
        update.setVault(smartContract.getVault());
        update.setAddresses(addresses);

        final var updated = getUnderTest().updateSmartContract(update);
        assertEquals(updated, update);

        final var old = smartContracts.put(updated.getId(), updated);
        assertNotNull(old);

    }

    @Test(dataProvider = "blockchainNetworks", groups = "read", dependsOnGroups = "update")
    public void testGetContractsByNetwork(final BlockchainNetwork network) {

        final var smartContracts = new PaginationWalker()
                .toList((offset, count) -> getUnderTest().getSmartContracts(offset, count, null, List.of(network)));

        for(var smartContract : smartContracts) {
            assertTrue(smartContract.getAddresses().containsKey(network));
        }

    }

    @Test(dataProvider = "blockchainApi", groups = "read", dependsOnGroups = "update")
    public void testGetContractsByApi(final BlockchainApi api) {

        final var contracts = new PaginationWalker()
                .toList((offset, count) -> getUnderTest().getSmartContracts(offset, count, api, null));

        final var count = contracts
                .stream()
                .flatMap(contract -> contract.getAddresses().keySet().stream())
                .map(BlockchainNetwork::api)
                .filter(a -> a.equals(api))
                .count();

        assertTrue(count > 0, "API Must Appear at least once.");

    }

    @Test(dataProvider = "smartContracts", groups = "read", dependsOnGroups = "update")
    public void testGetSingleSmartContractById(final SmartContract smartContract) {
        final var fetched = getUnderTest().getSmartContractById(smartContract.getId());
        assertEquals(fetched, smartContract);
    }

    @Test(dataProvider = "smartContracts", groups = "read", dependsOnGroups = "update")
    public void testGetSingleSmartContractByName(final SmartContract smartContract) {
        final var fetched = getUnderTest().getSmartContractByNameOrId(smartContract.getName());
        assertEquals(fetched, smartContract);
    }

    @Test(groups = "read", dependsOnGroups = "update", expectedExceptions = SmartContractNotFoundException.class)
    public void testGetSingleContractNotFound() {
        getUnderTest().getSmartContractById(new ObjectId().toString());
    }

    @Test(groups = "read", dependsOnGroups = "update")
    public void testFindSingleContractNotFound() {
        final var wallet = getUnderTest().findSmartContractById(new ObjectId().toString());
        assertFalse(wallet.isPresent());
    }

    @Test(dataProvider = "smartContracts", groups = "delete", dependsOnGroups = "read")
    public void deleteContract(final SmartContract smartContract) {
        getUnderTest().deleteContract(smartContract.getId());
    }

    @Test(dataProvider = "smartContracts",
            groups = "delete",
            dependsOnMethods = "deleteContract",
            expectedExceptions = SmartContractNotFoundException.class)
    public void doubleDeleteContractFails(final SmartContract smartContract) {
        getUnderTest().deleteContract(smartContract.getId());
    }

    public WalletDao getWalletDao() {
        return walletDao;
    }

    @Inject
    public void setWalletDao(WalletDao walletDao) {
        this.walletDao = walletDao;
    }

    public UserTestFactory getUserTestFactory() {
        return userTestFactory;
    }

    @Inject
    public void setUserTestFactory(UserTestFactory userTestFactory) {
        this.userTestFactory = userTestFactory;
    }

    public SmartContractDao getUnderTest() {
        return underTest;
    }

    @Inject
    public void setUnderTest(SmartContractDao underTest) {
        this.underTest = underTest;
    }

    public VaultDao getVaultDao() {
        return vaultDao;
    }

    @Inject
    public void setVaultDao(VaultDao vaultDao) {
        this.vaultDao = vaultDao;
    }

}
