package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.dao.VaultDao;
import com.namazustudios.socialengine.exception.blockchain.WalletNotFoundException;
import com.namazustudios.socialengine.model.blockchain.BlockchainApi;
import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
import com.namazustudios.socialengine.model.blockchain.wallet.Vault;
import com.namazustudios.socialengine.model.blockchain.wallet.Wallet;
import com.namazustudios.socialengine.model.blockchain.wallet.WalletAccount;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.util.PaginationWalker;
import org.bson.types.ObjectId;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.namazustudios.socialengine.rt.util.Hex.Case.UPPER;
import static com.namazustudios.socialengine.rt.util.Hex.forNibble;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.*;
import static org.testng.Assert.assertTrue;

@Guice(modules = IntegrationTestModule.class)
public class MongoVaultDaoTest {

    private static final int TEST_USER_COUNT = 10;

    private VaultDao underTest;

    private UserTestFactory userTestFactory;

    private User trudyUser;

    private List<User> regularUsers;

    private final Map<String, Vault> vaults = new ConcurrentHashMap<>();

    @BeforeClass
    public void createTestUsers() {

        trudyUser = getUserTestFactory().createTestUser();

        regularUsers = IntStream.range(0, TEST_USER_COUNT)
                .mapToObj(i -> getUserTestFactory().createTestUser())
                .collect(toList());

    }

    @DataProvider
    public Object[][] regularUsers() {
        return regularUsers
                .stream()
                .map(u -> new Object[]{u})
                .toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] regularUsersAndBlockchainProtocols() {
        return regularUsers
                .stream()
                .flatMap(user -> Stream
                        .of(BlockchainApi.values())
                        .map(protocol -> new Object[]{user, protocol}))
                .toArray(Object[][]::new);
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
        return vaults
                .values()
                .stream()
                .map(u -> new Object[]{u})
                .toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] vaultsById() {
        return vaults
                .entrySet()
                .stream()
                .map(e -> new Object[]{e.getKey(), e.getValue()})
                .toArray(Object[][]::new);
    }

    public static String randomKey() {

        final var key = new char[256];
        final var random = ThreadLocalRandom.current();

        for (int i = 0; i < key.length; ++i) {
            key[i] = forNibble(random.nextInt(0xF), UPPER);
        }

        return new String(key);

    }

    @Test(dataProvider = "regularUsersAndBlockchainNetworks", groups = "create")
    public void testCreateVaults(final User user, final BlockchainNetwork network) {

        final var vault = new Vault();
        wallet.setDisplayName("Wallet for User " + user.getName());
        wallet.setUser(user);
        wallet.setNetworks(List.of(network));
        wallet.setApi(network.api());

        final var identity = new WalletAccount();
        identity.setEncrypted(true);
        identity.setAddress(randomKey());
        identity.setPrivateKey(randomKey());
        wallet.setAccounts(List.of(identity));

        final var created = getUnderTest().createWallet(wallet);
        assertNotNull(created.getId());
        assertEquals(created.getDisplayName(), wallet.getDisplayName());
        assertEquals(created.getNetworks(), wallet.getNetworks());
        assertEquals(created.getUser(), wallet.getUser());
        assertEquals(created.getApi(), wallet.getApi());

        vaults.put(created.getId(), created);

    }

    @Test(dataProvider = "wallets", groups = "update", dependsOnGroups = "create")
    public void testUpdateWallet(final Wallet wallet) {

        final var update = new Wallet();
        update.setId(wallet.getId());
        update.setPreferredAccount(wallet.getPreferredAccount());
        update.setDisplayName(wallet.getDisplayName());
        update.setUser(wallet.getUser());
        update.setNetworks(wallet.getNetworks());
        update.setApi(wallet.getApi());

        final var identity = new WalletAccount();
        identity.setEncrypted(true);
        identity.setAddress(randomKey());
        identity.setPrivateKey(randomKey());
        update.setAccounts(List.of(identity));

        final var updated = getUnderTest().updateWallet(update);
        assertEquals(updated, update);
        assertNotEquals(updated, wallet);

        vaults.put(updated.getId(), updated);

    }

    @Test(dataProvider = "regularUsers", groups = "read", dependsOnGroups = "update")
    public void testGetWalletForUser(final User user) {

        final var wallets = new PaginationWalker()
                .toList((offset, count) -> getUnderTest()
                        .getWallets(offset, count, user.getId(), null, null));

        for(var wallet : wallets) {
            assertEquals(wallet.getUser(), user);
        }

    }

    @Test(dataProvider = "regularUsersAndBlockchainNetworks", groups = "read", dependsOnGroups = "update")
    public void testGetWalletForUser(final User user, final BlockchainNetwork network) {

        final var wallets = new PaginationWalker()
                .toList((offset, count) -> getUnderTest()
                        .getWallets(offset, count, user.getId(), null, List.of(network)));

        for(var wallet : wallets) {
            assertEquals(wallet.getUser(), user);
            assertEquals(wallet.getApi(), network.api());
            assertTrue(wallet.getNetworks().contains(network));
        }

    }

    @Test(dataProvider = "regularUsersAndBlockchainProtocols", groups = "read", dependsOnGroups = "update")
    public void testGetWalletForUser(final User user, final BlockchainApi protocol) {

        final var wallets = new PaginationWalker()
                .toList((offset, count) -> getUnderTest()
                        .getWallets(offset, count, user.getId(), protocol, null));

        for(var wallet : wallets) {
            assertEquals(wallet.getUser(), user);
            assertEquals(wallet.getApi(), protocol);
        }

    }

    @Test(dataProvider = "blockchainNetworks", groups = "read", dependsOnGroups = "update")
    public void testGetWalletNetwork(final BlockchainNetwork network) {

        final var wallets = new PaginationWalker()
                .toList((offset, count) -> getUnderTest()
                        .getWallets(offset, count, null, null, List.of(network))
                );

        for(var wallet : wallets) {
            assertTrue(wallet.getNetworks().contains(network));
            assertEquals(wallet.getApi(), network.api());
        }

    }

    @Test(dataProvider = "blockchainProtocols", groups = "read", dependsOnGroups = "update")
    public void testGetWalletProtocol(final BlockchainApi protocol) {

        final var wallets = new PaginationWalker()
                .toList((offset, count) -> getUnderTest()
                        .getWallets(offset, count, null, protocol, null));

        for(var wallet : wallets) {
            assertEquals(wallet.getApi(), protocol);
        }

    }

    @Test(groups = "read", dependsOnGroups = "update")
    public void testGetWalletWalletsFilters() {
        final var wallets = new PaginationWalker()
                .toList((offset, count) -> getUnderTest()
                        .getWallets(offset, count, trudyUser.getId(),null, null));
        assertTrue(wallets.isEmpty());
    }

    @Test(groups = "read", dependsOnGroups = "update")
    public void testEmptyNetworkReturnsNothing() {
        final var wallets = new PaginationWalker()
                .toList((offset, count) -> getUnderTest()
                        .getWallets(offset, count, null,null, new ArrayList<>()));
        assertTrue(wallets.isEmpty());
    }

    @Test(dataProvider = "walletsById", groups = "read", dependsOnGroups = "update")
    public void testGetSingleWallet(final String walletId, final Wallet wallet) {
        final var fetched = getUnderTest().getWallet(walletId);
        assertEquals(fetched, wallet);
    }

    @Test(dataProvider = "walletsById", groups = "read", dependsOnGroups = "update")
    public void testGetSingleWalletForUser(final String walletId, final Wallet wallet) {
        final var fetched = getUnderTest().getWallet(walletId, wallet.getUser().getId());
        assertEquals(fetched, wallet);
    }

    @Test(groups = "read", dependsOnGroups = "update", expectedExceptions = WalletNotFoundException.class)
    public void testGetSingleWalletNotFound() {
        getUnderTest().getWallet(new ObjectId().toString());
    }

    @Test(dataProvider = "wallets", groups = "read", dependsOnGroups = "update", expectedExceptions = WalletNotFoundException.class)
    public void testGetSingleWalletForUserNotFound(final Wallet wallet) {
        getUnderTest().getWallet(wallet.getId(), trudyUser.getId());
    }

    @Test(groups = "read", dependsOnGroups = "update")
    public void testFindSingleWalletNotFound() {
        final var wallet = getUnderTest().findWallet(new ObjectId().toString());
        assertFalse(wallet.isPresent());
    }

    @Test(dataProvider = "wallets", groups = "read", dependsOnGroups = "update")
    public void testFindSingleWalletForUserNotFound(final Wallet wallet) {
        final var result = getUnderTest().findWallet(wallet.getId(), trudyUser.getId());
        assertFalse(result.isPresent());
    }

    @Test(dataProvider = "wallets",
            groups = {"delete", "pre delete"},
            dependsOnGroups = "update",
            expectedExceptions = WalletNotFoundException.class)
    public void deleteWalletForWrongUserFails(final Wallet wallet) {
        try {
            getUnderTest().deleteWalletForUser(wallet.getId(), trudyUser.getId());
        } finally {
            assertTrue(getUnderTest().findWallet(wallet.getId()).isPresent());
        }
    }

    @Test(dataProvider = "wallets", groups = "delete", dependsOnGroups = "pre delete")
    public void deleteWallet(final Wallet wallet) {
        getUnderTest().deleteWallet(wallet.getId());
    }

    @Test(dataProvider = "wallets",
            groups = "delete",
            dependsOnGroups = "pre delete",
            dependsOnMethods = "deleteWallet",
            expectedExceptions = WalletNotFoundException.class)
    public void doubleDeleteWalletFails(final Wallet wallet) {
        getUnderTest().deleteWallet(wallet.getId());
    }

    @Test(dataProvider = "regularUsersAndBlockchainNetworks", groups = "delete", dependsOnGroups = "pre delete")
    public void deleteWalletForUser(final User user, final BlockchainNetwork network) {

        final var wallet = new Wallet();
        wallet.setDisplayName("Wallet for User " + user.getName());
        wallet.setUser(user);
        wallet.setNetworks(List.of(network));
        wallet.setApi(network.api());

        final var identity = new WalletAccount();
        identity.setEncrypted(false);
        identity.setAddress(randomKey());
        identity.setPrivateKey(randomKey());
        wallet.setAccounts(List.of(identity));

        final var created = getUnderTest().createWallet(wallet);
        getUnderTest().deleteWalletForUser(created.getId(), user.getId());
        assertTrue(getUnderTest().findWallet(created.getId()).isEmpty());

    }

    public VaultDao getUnderTest() {
        return underTest;
    }

    @Inject
    public void setUnderTest(VaultDao underTest) {
        this.underTest = underTest;
    }

    public UserTestFactory getUserTestFactory() {
        return userTestFactory;
    }

    @Inject
    public void setUserTestFactory(UserTestFactory userTestFactory) {
        this.userTestFactory = userTestFactory;
    }

}
