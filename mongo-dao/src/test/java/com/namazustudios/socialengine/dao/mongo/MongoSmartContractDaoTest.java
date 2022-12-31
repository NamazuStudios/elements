package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.dao.SmartContractDao;
import com.namazustudios.socialengine.dao.WalletDao;
import com.namazustudios.socialengine.exception.blockchain.WalletNotFoundException;
import com.namazustudios.socialengine.model.blockchain.BlockchainApi;
import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
import com.namazustudios.socialengine.model.blockchain.contract.SmartContract;
import com.namazustudios.socialengine.model.blockchain.contract.SmartContractAddress;
import com.namazustudios.socialengine.model.blockchain.wallet.Wallet;
import com.namazustudios.socialengine.model.blockchain.wallet.WalletIdentityPair;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.util.PaginationWalker;
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
import java.util.stream.Stream;

import static com.namazustudios.socialengine.model.user.User.Level.SUPERUSER;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.*;

@Guice(modules = IntegrationTestModule.class)
public class MongoSmartContractDaoTest {

    private SmartContractDao underTest;

    private WalletDao walletDao;

    private UserTestFactory userTestFactory;

    private User adminUser;

    private List<Wallet> wallets;

    private final Map<String, SmartContract> smartContracts = new ConcurrentHashMap<>();

    @BeforeClass
    public void createTestUsersAndWallet() {

        adminUser = getUserTestFactory().createTestUser(u -> u.setLevel(SUPERUSER));

        wallets = Stream.of(BlockchainApi.values())
                .map(api -> {

                    final var identity = new WalletIdentityPair();
                    identity.setEncrypted(false);
                    identity.setAddress(MongoWalletDaoTest.randomKey());
                    identity.setPrivateKey(MongoWalletDaoTest.randomKey());

                    final var wallet = new Wallet();
                    wallet.setApi(api);
                    wallet.setUser(adminUser);
                    wallet.setNetworks(api.networks().collect(toList()));
                    wallet.setDisplayName("Test Wallet: " + api);
                    wallet.setDefaultIdentity(0);
                    wallet.setEncryption(null);
                    wallet.setIdentities(new ArrayList<>(List.of(identity)));

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
    public Object[][] blockchainProtocols() {
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

    @Test(dataProvider = "wallets", groups = "create")
    public void testCreateSmartContract(final Wallet wallet) {

        final var addresses = new HashMap<BlockchainNetwork, SmartContractAddress>();

        wallet.getNetworks().forEach(network -> {
            final var address = new SmartContractAddress();
            address.setAddress(MongoWalletDaoTest.randomKey());
            addresses.put(network, address);
        });


        final var metadata = new HashMap<String, Object>();
        metadata.put("API", wallet.getApi());
        metadata.put("NETS", wallet.getNetworks());

        final var contract = new SmartContract();
        contract.setWallet(wallet);
        contract.setMetadata(metadata);
        contract.setAddresses(addresses);
        contract.setApi(wallet.getApi());
        contract.setDisplayName("Test Contract: " + wallet.getApi());

        final var created = getUnderTest().createSmartContract(contract);
        assertNotNull(created.getId());
        assertEquals(created.getWallet(), wallet);
        assertEquals(created.getDisplayName(), contract.getDisplayName());
        assertEquals(created.getApi(), contract.getApi());
        assertEquals(created.getAddresses(), contract.getAddresses());

        smartContracts.put(created.getId(), created);

    }

    @Test(dataProvider = "wallets", groups = "update", dependsOnGroups = "create")
    public void testUpdateSmartContract(final SmartContract smartContract) {

        final var addresses = new HashMap<BlockchainNetwork, SmartContractAddress>();

        smartContract.getAddresses().forEach((network, address) -> {
            final var newAddress = new SmartContractAddress();
            newAddress.setAddress(MongoWalletDaoTest.randomKey());
            addresses.put(network, address);
        });

        final var metadata = new HashMap<String, Object>();
        metadata.putAll(smartContract.getMetadata());

        final var update = new SmartContract();
        update.setId(smartContract.getId());
        update.setDisplayName(smartContract.getDisplayName());
        update.setApi(smartContract.getApi());
        update.setMetadata(smartContract.getMetadata());
        update.setWallet(smartContract.getWallet());
        update.setAddresses(addresses);

        final var updated = getUnderTest().updateSmartContract(update);
        assertEquals(updated, update);

        final var old = smartContracts.put(updated.getId(), updated);
        assertNotNull(old);

    }

    @Test(dataProvider = "blockchainNetworks", groups = "read", dependsOnGroups = "update")
    public void testGetContractsByNetwork(final BlockchainNetwork network) {

        final var smartContracts = new PaginationWalker()
                .toList((offset, count) -> getUnderTest()
                        .getSmartContracts(offset, count, null, List.of(network))
                );

        for(var smartContract : smartContracts) {
            assertTrue(smartContract.getAddresses().keySet().contains(network));
            assertEquals(smartContract.getApi(), network.api());
        }

    }

    @Test(dataProvider = "blockchainProtocols", groups = "read", dependsOnGroups = "update")
    public void testGetContractsByProtocol(final BlockchainApi protocol) {

        final var wallets = new PaginationWalker()
                .toList((offset, count) -> getUnderTest()
                        .getSmartContracts(offset, count, protocol, null));

        for(var wallet : wallets) {
            assertEquals(wallet.getApi(), protocol);
        }

    }

    @Test(groups = "read", dependsOnGroups = "update")
    public void testEmptyNetworkReturnsNothing() {
        final var smartContracts = new PaginationWalker()
                .toList((offset, count) -> getUnderTest()
                        .getSmartContracts(offset, count, null, new ArrayList<>()));
        assertTrue(smartContracts.isEmpty());
    }

    @Test(dataProvider = "smartContracts", groups = "read", dependsOnGroups = "update")
    public void testGetSingleSmartContract(final SmartContract smartContract) {
        final var fetched = getUnderTest().getSmartContract(smartContract.getId());
        assertEquals(fetched, smartContract);
    }

    @Test(groups = "read", dependsOnGroups = "update", expectedExceptions = WalletNotFoundException.class)
    public void testGetSingleWalletNotFound() {
        getUnderTest().getSmartContract(new ObjectId().toString());
    }

    @Test(groups = "read", dependsOnGroups = "update")
    public void testFindSingleWalletNotFound() {
        final var wallet = getUnderTest().findSmartContract(new ObjectId().toString());
        assertFalse(wallet.isPresent());
    }

    @Test(dataProvider = "smartContracts", groups = "delete", dependsOnGroups = "read")
    public void deleteWallet(final SmartContract smartContract) {
        getUnderTest().deleteContract(smartContract.getId());
    }

    @Test(dataProvider = "smartContracts",
            groups = "delete",
            dependsOnMethods = "deleteWallet",
            expectedExceptions = WalletNotFoundException.class)
    public void doubleDeleteWalletFails(final SmartContract smartContract) {
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

}
