package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.model.blockchain.BlockchainApi;
import com.namazustudios.socialengine.model.blockchain.wallet.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.namazustudios.socialengine.Headers.SESSION_SECRET;
import static com.namazustudios.socialengine.model.crypto.PrivateKeyCrytpoAlgorithm.RSA_512;
import static com.namazustudios.socialengine.rest.TestUtils.TEST_API_ROOT;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class UserWalletApiTest {

    @Factory
    public Object[] getTests() {
        return new Object[] {
                TestUtils.getInstance().getXodusTest(UserWalletApiTest.class),
                TestUtils.getInstance().getUnixFSTest(UserWalletApiTest.class)
        };
    }

    @Inject
    private Client client;

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private ClientContext userClientContext;

    @Inject
    private ClientContext trudyClientContext;

    private Vault vault;

    private Vault emptyVault;

    @DataProvider
    private Object[][] blockchainApis() {
        return Stream.of(BlockchainApi.values())
                .map(network -> new Object[]{network})
                .toArray(Object[][]::new);
    }

    @BeforeClass
    private void setUp() {
        userClientContext.createUser("vaultboy").createSession();
        trudyClientContext.createUser("trudyuser").createSession();
        vault = createVault(userClientContext);
        emptyVault = createVault(trudyClientContext);
    }

    private Vault createVault(final ClientContext clientContext) {

        final var toCreate = new CreateVaultRequest();
        toCreate.setAlgorithm(RSA_512);
        toCreate.setUserId(clientContext.getUser().getId());
        toCreate.setPassphrase(clientContext.getUser().getName());
        toCreate.setDisplayName(format("Vault for %s (Encrypted)", clientContext.getUser().getName()));

        final var response = client
                .target(apiRoot + "/blockchain/omni/vault")
                .request()
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .post(Entity.entity(toCreate, APPLICATION_JSON));

        assertEquals(response.getStatus(), 200);
        return response.readEntity(Vault.class);

    }

    @Test(groups = "create", dataProvider = "blockchainApis")
    public void testCreateWallet(final BlockchainApi api) {

        final var account = new WalletAccount();
        account.setEncrypted(false);
        account.setAddress("Random Address");
        account.setPrivateKey("Random Private Key");

        final var toCreate = new CreateWalletRequest();
        toCreate.setApi(api);
        toCreate.setDisplayName("Wallet for " + api + " for user " + userClientContext.getUser().getName());
        toCreate.setPreferredAccount(0);
        toCreate.setNetworks(api.networks().collect(toList()));
        toCreate.setAccounts(List.of(account));

        final var response = client
                .target(format("%s/blockchain/omni/vault/%s/wallet", apiRoot, vault.getId()))
                .request()
                .header(SESSION_SECRET, userClientContext.getSessionSecret())
                .post(Entity.entity(toCreate, APPLICATION_JSON));

        assertEquals(response.getStatus(), 200);

        final var wallet = response.readEntity(Wallet.class);
        assertNotNull(wallet.getId());
        assertEquals(wallet.getApi(), api);
        assertEquals(wallet.getNetworks(), toCreate.getNetworks());
        assertEquals(wallet.getDisplayName(), toCreate.getDisplayName());
        assertEquals(wallet.getPreferredAccount(), toCreate.getPreferredAccount());
        assertEquals(wallet.getVault(), vault);

    }

    @Test(groups = "update", dependsOnGroups = "create")
    public void testUpdateVault() {
        final var toUpdate = new UpdateVaultRequest();
        toUpdate.setUserId(vault.getUser().getId());
        toUpdate.setDisplayName("Wallet for " + api + " for user " + userClientContext.getUser().getName());
    }

    @Test(groups = "update", dependsOnGroups = "create")
    public void testStealVaultFails() {

    }

    @Test(groups = "update", dependsOnGroups = "create")
    public void testTransferVaultFails() {

    }

    @Test(groups = "read", dependsOnGroups = "update")
    public void testGetWalletsForVault() {

    }

    @Test(groups = "read", dependsOnGroups = "update")
    public void testGetWalletsForEmptyVault() {

    }

}
