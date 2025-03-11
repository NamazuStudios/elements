package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.dao.VaultDao;
import dev.getelements.elements.sdk.model.exception.blockchain.VaultNotFoundException;
import dev.getelements.elements.sdk.model.blockchain.BlockchainApi;
import dev.getelements.elements.sdk.model.blockchain.wallet.Vault;
import dev.getelements.elements.sdk.model.blockchain.wallet.VaultKey;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.util.PaginationWalker;
import org.bson.types.ObjectId;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static dev.getelements.elements.sdk.model.crypto.PrivateKeyCrytpoAlgorithm.RSA_512;
import static dev.getelements.elements.sdk.util.Hex.Case.UPPER;
import static dev.getelements.elements.sdk.util.Hex.forNibble;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.*;

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
    public Object[][] blockchainProtocols() {
        return Stream
                .of(BlockchainApi.values())
                .map(protocol -> new Object[]{protocol})
                .toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] vaults() {
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

    @Test(dataProvider = "regularUsers", groups = "create")
    public void testCreateVaults(final User user) {

        final var encryption = new HashMap<String, Object>();
        encryption.put("Fake Encryption Key", "Fake Encryption Value");

        final var key = new VaultKey();
        key.setEncrypted(true);
        key.setEncryption(encryption);
        key.setAlgorithm(RSA_512);
        key.setPublicKey(MongoWalletDaoTest.randomKey());
        key.setPrivateKey(MongoWalletDaoTest.randomKey());

        final var vault = new Vault();
        vault.setDisplayName("Wallet for User " + user.getName());
        vault.setUser(user);
        vault.setKey(key);

        final var created = getUnderTest().createVault(vault);
        assertNotNull(created.getId());
        assertEquals(created.getDisplayName(), vault.getDisplayName());
        assertEquals(created.getUser(), vault.getUser());
        assertEquals(created.getKey(), vault.getKey());

        vaults.put(created.getId(), created);

    }

    @Test(dataProvider = "vaults", groups = "update", dependsOnGroups = "create")
    public void testUpdateVaults(final Vault vault) {

        final var encryption = new HashMap<String, Object>();
        encryption.put("Fake Encryption Key", "Fake Encryption Value");

        final var key = new VaultKey();
        key.setEncrypted(true);
        key.setEncryption(encryption);
        key.setAlgorithm(RSA_512);
        key.setPublicKey(MongoWalletDaoTest.randomKey());
        key.setPrivateKey(MongoWalletDaoTest.randomKey());

        final var update = new Vault();
        update.setKey(key);
        update.setId(vault.getId());
        update.setDisplayName(vault.getDisplayName());
        update.setUser(vault.getUser());

        final var updated = getUnderTest().updateVault(update);
        assertEquals(updated, update);
        assertNotEquals(updated, vault);

        vaults.put(updated.getId(), updated);

    }

    @Test(dataProvider = "regularUsers", groups = "read", dependsOnGroups = "update")
    public void testGetVaultForUser(final User user) {

        final var vaults = new PaginationWalker()
                .toList((offset, count) -> getUnderTest().getVaults(offset, count, user.getId()));

        for(var vault : vaults) {
            assertEquals(vault.getUser(), user);
        }

    }

    @Test(groups = "read", dependsOnGroups = "update")
    public void testGetVaultFilters() {
        final var vaults = new PaginationWalker()
                .toList((offset, count) -> getUnderTest().getVaults(offset, count, trudyUser.getId()));
        assertTrue(vaults.isEmpty());
    }

    @Test(dataProvider = "vaultsById", groups = "read", dependsOnGroups = "update")
    public void testGetSingleVault(final String vaultId, final Vault vault) {
        final var fetched = getUnderTest().getVault(vaultId);
        assertEquals(fetched, vault);
    }

    @Test(dataProvider = "vaultsById", groups = "read", dependsOnGroups = "update")
    public void testGetSingleVaultForUser(final String vaultId, final Vault vault) {
        final var fetched = getUnderTest().getVaultForUser(vaultId, vault.getUser().getId());
        assertEquals(fetched, vault);
    }

    @Test(groups = "read", dependsOnGroups = "update", expectedExceptions = VaultNotFoundException.class)
    public void testGetSingleVaultNotFound() {
        getUnderTest().getVault(new ObjectId().toString());
    }

    @Test(dataProvider = "vaults", groups = "read", dependsOnGroups = "update", expectedExceptions = VaultNotFoundException.class)
    public void testGetSingleVaultForUserNotFound(final Vault vault) {
        getUnderTest().getVaultForUser(vault.getId(), trudyUser.getId());
    }

    @Test(groups = "read", dependsOnGroups = "update")
    public void testFindSingleVaultNotFound() {
        final var vault = getUnderTest().findVault(new ObjectId().toString());
        assertFalse(vault.isPresent());
    }

    @Test(dataProvider = "vaults", groups = "read", dependsOnGroups = "update")
    public void testFindSingleWalletForUserNotFound(final Vault vault) {
        final var result = getUnderTest().findVaultForUser(vault.getId(), trudyUser.getId());
        assertFalse(result.isPresent());
    }

    @Test(dataProvider = "vaults",
            groups = {"delete", "pre delete"},
            dependsOnGroups = "update",
            expectedExceptions = VaultNotFoundException.class)
    public void deleteVaultForWrongUserFails(final Vault vault) {
        try {
            getUnderTest().deleteVaultForUser(vault.getId(), trudyUser.getId());
        } finally {
            assertTrue(getUnderTest().findVault(vault.getId()).isPresent());
        }
    }

    @Test(dataProvider = "vaults", groups = "delete", dependsOnGroups = "pre delete")
    public void deleteVault(final Vault vault) {
        getUnderTest().deleteVault(vault.getId());
    }

    @Test(dataProvider = "vaults",
            groups = "delete",
            dependsOnGroups = "pre delete",
            dependsOnMethods = "deleteVault",
            expectedExceptions = VaultNotFoundException.class)
    public void doubleDeleteVaultFails(final Vault vault) {
        getUnderTest().deleteVault(vault.getId());
    }

    @Test(dataProvider = "regularUsers", groups = "delete", dependsOnGroups = "pre delete")
    public void deleteVaultForUser(final User user) {

        final var encryption = new HashMap<String, Object>();
        encryption.put("Fake Encryption Key", "Fake Encryption Value");

        final var key = new VaultKey();
        key.setEncrypted(true);
        key.setEncryption(encryption);
        key.setAlgorithm(RSA_512);
        key.setPublicKey(MongoWalletDaoTest.randomKey());
        key.setPrivateKey(MongoWalletDaoTest.randomKey());

        final var vault = new Vault();
        vault.setDisplayName("Wallet for User " + user.getName());
        vault.setUser(user);
        vault.setKey(key);

        final var created = getUnderTest().createVault(vault);
        assertNotNull(created.getId());
        assertEquals(created.getDisplayName(), vault.getDisplayName());
        assertEquals(created.getUser(), vault.getUser());
        assertEquals(created.getKey(), vault.getKey());

        getUnderTest().deleteVaultForUser(created.getId(), user.getId());
        assertTrue(getUnderTest().findVault(created.getId()).isEmpty());

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
