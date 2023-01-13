package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.model.blockchain.wallet.CreateVaultRequest;
import com.namazustudios.socialengine.model.blockchain.wallet.Vault;
import com.namazustudios.socialengine.model.crypto.PrivateKeyCrytpoAlgorithm;
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
        return Stream.of(PrivateKeyCrytpoAlgorithm.values())
                .map(algo -> new Object[]{algo})
                .toArray(Object[][]::new);
    }

    @BeforeClass
    public void setupUser() {
        userClientContext.createUser("vaultboy").createSession();
        trudyClientContext.createUser("trudyuser").createSession();
    }

    @Test(invocationCount = 5, dataProvider = "encryptionAlgorithms", groups = "create")
    public void testCreateEncryptedVaults(final PrivateKeyCrytpoAlgorithm algorithm) {

        final var toCreate = new CreateVaultRequest();
        toCreate.setAlgorithm(algorithm);
        toCreate.setUserId(userClientContext.getUser().getId());
        toCreate.setPassphrase(userClientContext.getUser().getName());
        toCreate.setDisplayName(format("Vault for %s", userClientContext.getUser().getName()));

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
        assertEquals(key.getAlgorithm(), algorithm);

        encryptedVaults.put(vault.getId(), vault);

    }

    @Test(invocationCount = 5, dataProvider = "encryptionAlgorithms", groups = "create")
    public void testCreateUnencryptedVaults(final PrivateKeyCrytpoAlgorithm algorithm) {

        final var toCreate = new CreateVaultRequest();
        toCreate.setAlgorithm(algorithm);
        toCreate.setUserId(userClientContext.getUser().getId());
        toCreate.setPassphrase("");
        toCreate.setDisplayName(format("Vault for %s", userClientContext.getUser().getName()));

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
        assertFalse(key.isEncrypted());
        assertNotNull(key.getPublicKey());
        assertNotNull(key.getPrivateKey());
        assertEquals(key.getAlgorithm(), algorithm);

        unencryptedVaults.put(vault.getId(), vault);

    }


}
