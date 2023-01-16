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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static com.namazustudios.socialengine.Headers.SESSION_SECRET;
import static com.namazustudios.socialengine.model.blockchain.BlockchainApi.SOLANA;
import static com.namazustudios.socialengine.model.crypto.PrivateKeyCrytpoAlgorithm.RSA_512;
import static com.namazustudios.socialengine.rest.TestUtils.TEST_API_ROOT;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.testng.Assert.*;

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

    private Vault trudyVault;

    private final Map<String, Wallet> wallets = new ConcurrentHashMap<>();

    @DataProvider
    private Object[][] blockchainApis() {
        return Stream.of(BlockchainApi.values())
                .map(network -> new Object[]{network})
                .toArray(Object[][]::new);
    }

    @DataProvider
    private Object[][] walletsById() {
        return wallets
                .entrySet()
                .stream()
                .map(e -> new Object[]{e.getKey(), e.getValue()})
                .toArray(Object[][]::new);
    }

    @BeforeClass
    private void setUp() {
        userClientContext.createUser("vaultboy").createSession();
        trudyClientContext.createUser("trudyuser").createSession();
        vault = createVault(userClientContext);
        emptyVault = createVault(userClientContext);
        trudyVault = createVault(trudyClientContext);
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

        final var firstAccount = new CreateWalletRequestAccount();
        firstAccount.setGenerate(false);
        firstAccount.setAddress("First Random Address");
        firstAccount.setPrivateKey("First Random Private Key");

        final var secondAccount = new CreateWalletRequestAccount();

        if (SOLANA.equals(api)) {
            secondAccount.setGenerate(false);
            secondAccount.setAddress("Second Random Address");
            secondAccount.setPrivateKey("Second Random Private Key");
        } else {
            secondAccount.setGenerate(true);
        }

        final var toCreate = new CreateWalletRequest();
        toCreate.setApi(api);
        toCreate.setDisplayName("Wallet for " + api + " for user " + userClientContext.getUser().getName());
        toCreate.setPreferredAccount(0);
        toCreate.setNetworks(api.networks().collect(toList()));
        toCreate.setAccounts(List.of(firstAccount, secondAccount));

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

        final var existing = wallets.put(wallet.getId(), wallet);
        assertNull(existing);

    }

    @Test(groups = "update", dependsOnGroups = "create", dataProvider = "walletsById")
    public void testUpdateWallet(final String walletId, final Wallet wallet) {
        final var toUpdate = new UpdateWalletRequest();
        toUpdate.setDefaultIdentity(1);
        toUpdate.setDisplayName(format("Vault for %s (Encrypted) (Updated)", userClientContext.getUser().getName()));
    }

    @Test(groups = "read", dependsOnGroups = "update")
    public void testGetWalletsForVault() {

    }

    @Test(groups = "read", dependsOnGroups = "update")
    public void testGetWalletsForEmptyVault() {

    }

}
