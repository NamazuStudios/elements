package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.model.blockchain.wallet.CreateVaultRequest;
import com.namazustudios.socialengine.model.blockchain.wallet.UpdateVaultRequest;
import com.namazustudios.socialengine.model.blockchain.wallet.Vault;
import com.namazustudios.socialengine.model.crypto.PrivateKeyCrytpoAlgorithm;
import com.namazustudios.socialengine.rest.model.VaultPagination;
import com.namazustudios.socialengine.util.PaginationWalker;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static com.namazustudios.socialengine.Headers.SESSION_SECRET;
import static com.namazustudios.socialengine.rest.TestUtils.TEST_API_ROOT;
import static com.namazustudios.socialengine.service.VaultService.DEFAULT_VAULT_ALGORITHM;
import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.testng.Assert.*;
import static org.testng.AssertJUnit.assertTrue;

public class UserVaultApiTest {

    @Factory
    public Object[] getTests() {
        return new Object[] {
                TestUtils.getInstance().getXodusTest(UserVaultApiTest.class),
                TestUtils.getInstance().getUnixFSTest(UserVaultApiTest.class)
        };
    }

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private Client client;

    @Inject
    private ClientContext userClientContext;

    @Inject
    private ClientContext trudyClientContext;

    private Map<String, Vault> encryptedVaults = new ConcurrentHashMap<>();

    private Map<String, Vault> unencryptedVaults = new ConcurrentHashMap<>();

    @DataProvider
    public Object[][] encryptionAlgorithms() {
        return Stream.concat(Stream.of(PrivateKeyCrytpoAlgorithm.values()), Stream.of((PrivateKeyCrytpoAlgorithm)null))
                .map(algo -> new Object[]{algo})
                .toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] encryptedVaultsById() {
        return encryptedVaults
                .entrySet()
                .stream()
                .map(e -> new Object[]{e.getKey(), e.getValue()})
                .toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] unencryptedVaultsById() {
        return unencryptedVaults
                .entrySet()
                .stream()
                .map(e -> new Object[]{e.getKey(), e.getValue()})
                .toArray(Object[][]::new);
    }

    @BeforeClass
    public void setupUser() {
        userClientContext.createUser("vaultboy").createSession();
        trudyClientContext.createUser("trudyuser").createSession();
    }

    @Test(dataProvider = "encryptionAlgorithms", groups = "create")
    public void testCreateTrudysVaults(final PrivateKeyCrytpoAlgorithm algorithm) {

        final var toCreate = new CreateVaultRequest();
        toCreate.setAlgorithm(algorithm);
        toCreate.setUserId(userClientContext.getUser().getId());
        toCreate.setPassphrase(userClientContext.getUser().getName());
        toCreate.setDisplayName(format("Vault for %s (Encrypted)", trudyClientContext.getUser().getName()));

        final var response = client
                .target(apiRoot + "/blockchain/omni/vault")
                .request()
                .header(SESSION_SECRET, userClientContext.getSessionSecret())
                .post(Entity.entity(toCreate, APPLICATION_JSON));

        assertEquals(response.getStatus(), 200);

        final var vault = response.readEntity(Vault.class);
        assertEquals(vault.getUser().getId(), userClientContext.getUser().getId());
        assertEquals(vault.getDisplayName(), toCreate.getDisplayName());

        final var key = vault.getKey();
        assertNotNull(key);
        assertTrue(key.isEncrypted());
        assertNotNull(key.getPublicKey());
        assertNotNull(key.getPrivateKey());
        assertEquals(key.getAlgorithm(), algorithm == null ? DEFAULT_VAULT_ALGORITHM : algorithm);

    }

    @Test(invocationCount = 5, dataProvider = "encryptionAlgorithms", groups = "create")
    public void testCreateEncryptedVaults(final PrivateKeyCrytpoAlgorithm algorithm) {

        final var toCreate = new CreateVaultRequest();
        toCreate.setAlgorithm(algorithm);
        toCreate.setUserId(userClientContext.getUser().getId());
        toCreate.setPassphrase(userClientContext.getUser().getName());
        toCreate.setDisplayName(format("Vault for %s (Encrypted)", userClientContext.getUser().getName()));

        final var response = client
                .target(apiRoot + "/blockchain/omni/vault")
                .request()
                .header(SESSION_SECRET, userClientContext.getSessionSecret())
                .post(Entity.entity(toCreate, APPLICATION_JSON));

        assertEquals(response.getStatus(), 200);

        final var vault = response.readEntity(Vault.class);
        assertEquals(vault.getUser().getId(), userClientContext.getUser().getId());
        assertEquals(vault.getDisplayName(), toCreate.getDisplayName());

        final var key = vault.getKey();
        assertNotNull(key);
        assertTrue(key.isEncrypted());
        assertNotNull(key.getPublicKey());
        assertNotNull(key.getPrivateKey());
        assertEquals(key.getAlgorithm(), algorithm == null ? DEFAULT_VAULT_ALGORITHM : algorithm);

        encryptedVaults.put(vault.getId(), vault);

    }

    @Test(invocationCount = 5, dataProvider = "encryptionAlgorithms", groups = "create")
    public void testCreateUnencryptedVaults(final PrivateKeyCrytpoAlgorithm algorithm) {

        final var toCreate = new CreateVaultRequest();
        toCreate.setAlgorithm(algorithm);
        toCreate.setUserId(userClientContext.getUser().getId());
        toCreate.setPassphrase(null);
        toCreate.setDisplayName(format("Vault for %s (Unencrypted)", userClientContext.getUser().getName()));

        final var response = client
                .target(apiRoot + "/blockchain/omni/vault")
                .request()
                .header(SESSION_SECRET, userClientContext.getSessionSecret())
                .post(Entity.entity(toCreate, APPLICATION_JSON));

        assertEquals(response.getStatus(), 200);

        final var vault = response.readEntity(Vault.class);
        assertNotNull(vault.getId());
        assertEquals(vault.getUser().getId(), userClientContext.getUser().getId());
        assertEquals(vault.getDisplayName(), toCreate.getDisplayName());

        final var key = vault.getKey();
        assertNotNull(key);
        assertFalse(key.isEncrypted());
        assertNotNull(key.getPublicKey());
        assertNotNull(key.getPrivateKey());
        assertEquals(key.getAlgorithm(), algorithm == null ? DEFAULT_VAULT_ALGORITHM : algorithm);

        unencryptedVaults.put(vault.getId(), vault);

    }

    @Test(dataProvider = "encryptedVaultsById", groups = {"encrypt", "update"}, dependsOnGroups = "create")
    public void testReEncryptEncrypted(final String id, final Vault vault) {

        final var toUpdate = new UpdateVaultRequest();
        toUpdate.setUserId(vault.getUser().getId());
        toUpdate.setDisplayName("Vault for (Re-Encrypted): " + vault.getUser().getName());
        toUpdate.setPassphrase(vault.getUser().getName());
        toUpdate.setNewPassphrase(vault.getUser().getId() + "1");

        final var response = client
                .target(apiRoot + "/blockchain/omni/vault/" + id)
                .request()
                .header(SESSION_SECRET, userClientContext.getSessionSecret())
                .put(Entity.entity(toUpdate, APPLICATION_JSON));

        assertEquals(response.getStatus(), 200);

        final var updated = response.readEntity(Vault.class);
        assertNotNull(updated.getId());
        assertEquals(updated.getId(), id);
        assertEquals(updated.getUser().getId(), userClientContext.getUser().getId());
        assertEquals(updated.getDisplayName(), toUpdate.getDisplayName());

        final var key = updated.getKey();
        assertNotNull(key);
        assertTrue(key.isEncrypted());
        assertEquals(key.getPublicKey(), vault.getKey().getPublicKey());
        assertNotEquals(key.getPrivateKey(), vault.getKey().getPrivateKey());
        assertEquals(key.getAlgorithm(), vault.getKey().getAlgorithm());

        final var existing = encryptedVaults.put(id, vault);
        assertNotNull(existing);

    }

    @Test(dataProvider = "encryptedVaultsById", groups = "update", dependsOnGroups = "create")
    public void testStealVaultFails(final String id, final Vault vault) {

        final var toUpdate = new UpdateVaultRequest();
        toUpdate.setUserId(trudyClientContext.getUser().getId());
        toUpdate.setDisplayName("Vault for (Re-Encrypted): " + vault.getUser().getName());
        toUpdate.setPassphrase(vault.getUser().getName());
        toUpdate.setNewPassphrase(vault.getUser().getId() + "1");

        final var response = client
                .target(apiRoot + "/blockchain/omni/vault/" + id)
                .request()
                .header(SESSION_SECRET, trudyClientContext.getSessionSecret())
                .put(Entity.entity(toUpdate, APPLICATION_JSON));

        assertEquals(response.getStatus(), 400);

    }

    @Test(dataProvider = "encryptedVaultsById", groups = "update", dependsOnGroups = "create")
    public void testTransferVaultFails(final String id, final Vault vault) {

        final var toUpdate = new UpdateVaultRequest();
        toUpdate.setUserId(trudyClientContext.getUser().getId());
        toUpdate.setDisplayName("Vault for (Re-Encrypted): " + vault.getUser().getName());
        toUpdate.setPassphrase(vault.getUser().getName());
        toUpdate.setNewPassphrase(vault.getUser().getId() + "1");

        final var response = client
                .target(apiRoot + "/blockchain/omni/vault/" + id)
                .request()
                .header(SESSION_SECRET, userClientContext.getSessionSecret())
                .put(Entity.entity(toUpdate, APPLICATION_JSON));

        assertEquals(response.getStatus(), 400);

    }

    @Test(dataProvider = "unencryptedVaultsById", groups = {"encrypt", "update"}, dependsOnGroups = "create")
    public void testEncryptUnencrypted(final String id, final Vault vault) {

        final var toUpdate = new UpdateVaultRequest();
        toUpdate.setUserId(vault.getUser().getId());
        toUpdate.setDisplayName("Vault for (Encrypted): " + vault.getUser().getName());
        toUpdate.setNewPassphrase(vault.getUser().getId() + "1");

        final var response = client
                .target(apiRoot + "/blockchain/omni/vault/" + id)
                .request()
                .header(SESSION_SECRET, userClientContext.getSessionSecret())
                .put(Entity.entity(toUpdate, APPLICATION_JSON));

        assertEquals(response.getStatus(), 200);

        final var updated = response.readEntity(Vault.class);
        assertNotNull(updated.getId());
        assertEquals(updated.getId(), id);
        assertEquals(updated.getUser().getId(), userClientContext.getUser().getId());
        assertEquals(updated.getDisplayName(), toUpdate.getDisplayName());

        final var key = updated.getKey();
        assertNotNull(key);
        assertTrue(key.isEncrypted());
        assertEquals(key.getPublicKey(), vault.getKey().getPublicKey());
        assertNotEquals(key.getPrivateKey(), vault.getKey().getPrivateKey());
        assertEquals(key.getAlgorithm(), vault.getKey().getAlgorithm());

        final var existing = unencryptedVaults.put(id, vault);
        assertNotNull(existing);

    }

    @Test(groups = "read", dependsOnGroups = {"update", "encrypt"})
    public void testGetVaults() {

        final var vaults = new PaginationWalker().toList(((offset, count) -> client
                .target(format("%s/blockchain/omni/vault?offset=%d&count=%d", apiRoot, offset, count))
                .request()
                .header(SESSION_SECRET, userClientContext.getSessionSecret())
                .get(VaultPagination.class)
        ));

        final var userId = userClientContext.getUser().getId();
        vaults.forEach(vault -> assertEquals(userId, vault.getUser().getId()));

    }

    @Test(dataProvider = "encryptedVaultsById", groups = "read", dependsOnGroups = {"update", "encrypt"})
    public void testGetVaultsAllowsForCorrectUser(final String vaultId, final Vault vault) {

        final var response = client
                .target(format("%s/blockchain/omni/vault/%s", apiRoot, vaultId))
                .request()
                .header(SESSION_SECRET, userClientContext.getSessionSecret())
                .get();

        assertEquals(response.getStatus(), 200);

    }

    @Test(dataProvider = "encryptedVaultsById", groups = "read", dependsOnGroups = {"update", "encrypt"})
    public void testGetVaultsDeniesForWrongUser(final String vaultId, final Vault vault) {

        final var response = client
                .target(format("%s/blockchain/omni/vault/%s", apiRoot, vaultId))
                .request()
                .header(SESSION_SECRET, trudyClientContext.getSessionSecret())
                .get();

        assertEquals(response.getStatus(), 404);

    }

    @Test(dataProvider = "encryptedVaultsById",
            groups = "delete",
            dependsOnGroups = {"read"})
    public void testDeleteVaultDeniesForWrongUser(final String vaultId, final Vault vault) {

        final var response = client
                .target(format("%s/blockchain/omni/vault/%s", apiRoot, vaultId))
                .request()
                .header(SESSION_SECRET, trudyClientContext.getSessionSecret())
                .delete();

        assertEquals(response.getStatus(), 404);

    }

    @Test(dataProvider = "encryptedVaultsById",
            groups = "delete",
            dependsOnGroups = {"read"},
            dependsOnMethods = "testDeleteVaultDeniesForWrongUser")
    public void testDeleteVaultSucceedsForCorrectUser(final String vaultId, final Vault vault) {

        final var response = client
                .target(format("%s/blockchain/omni/vault/%s", apiRoot, vaultId))
                .request()
                .header(SESSION_SECRET, userClientContext.getSessionSecret())
                .delete();

        assertEquals(response.getStatus(), 204);

    }

    @Test(dataProvider = "encryptedVaultsById",
            groups = "delete",
            dependsOnGroups = {"read"},
            dependsOnMethods = "testDeleteVaultSucceedsForCorrectUser")
    public void testDeleteDoubleDeleteFails(final String vaultId, final Vault vault) {

        final var response = client
                .target(format("%s/blockchain/omni/vault/%s", apiRoot, vaultId))
                .request()
                .header(SESSION_SECRET, userClientContext.getSessionSecret())
                .delete();

        assertEquals(response.getStatus(), 404);

    }

    @Test(dataProvider = "encryptedVaultsById",
            groups = "delete",
            dependsOnGroups = {"read"},
            dependsOnMethods = "testDeleteVaultSucceedsForCorrectUser")
    public void testDeleteActuallyDeletes(final String vaultId, final Vault vault) {

        final var response = client
                .target(format("%s/blockchain/omni/vault/%s", apiRoot, vaultId))
                .request()
                .header(SESSION_SECRET, userClientContext.getSessionSecret())
                .get();

        assertEquals(response.getStatus(), 404);

    }

}
