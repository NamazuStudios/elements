package dev.getelements.elements.rest.test;

import dev.getelements.elements.sdk.model.blockchain.BlockchainNetwork;
import dev.getelements.elements.sdk.model.blockchain.contract.CreateSmartContractRequest;
import dev.getelements.elements.sdk.model.blockchain.contract.SmartContract;
import dev.getelements.elements.sdk.model.blockchain.contract.SmartContractAddress;
import dev.getelements.elements.sdk.model.blockchain.contract.UpdateSmartContractRequest;
import dev.getelements.elements.sdk.model.blockchain.wallet.CreateVaultRequest;
import dev.getelements.elements.sdk.model.blockchain.wallet.Vault;
import dev.getelements.elements.rest.test.model.SmartContractPagination;
import dev.getelements.elements.sdk.model.util.PaginationWalker;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import static dev.getelements.elements.sdk.model.Headers.SESSION_SECRET;
import static dev.getelements.elements.sdk.model.crypto.PrivateKeyCrytpoAlgorithm.RSA_512;
import static dev.getelements.elements.rest.test.TestUtils.TEST_API_ROOT;
import static java.lang.String.format;
import static java.util.stream.Collectors.toMap;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.testng.Assert.*;

public class SuperUserSmartContractApiTest {

    @Factory
    public Object[] getTests() {
        return new Object[] {
                TestUtils.getInstance().getTestFixture(SuperUserSmartContractApiTest.class)
        };
    }

    @Inject
    private Client client;

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private ClientContext clientContext;

    private Vault vault;

    private final Map<String, SmartContract> contracts = new ConcurrentHashMap<>();

    @DataProvider
    public Object[][] sequence() {
        return IntStream.range(0, 10)
                .mapToObj(i -> new Object[]{i})
                .toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] contractsById() {
        return contracts.entrySet()
                .stream()
                .map(e -> new Object[]{e.getKey(), e.getValue()})
                .toArray(Object[][]::new);
    }

    @BeforeClass
    public void setUp() {

        clientContext.createSuperuser("admin").createSession();

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
        vault = response.readEntity(Vault.class);

    }

    @Test(dataProvider = "sequence", groups = "create")
    public void createSmartContract(final int sequence) {

        final var addresses = Arrays.stream(BlockchainNetwork.values())
                .collect(toMap(net -> net, net -> {
                    final var sca = new SmartContractAddress();
                    sca.setAddress(format("Address for %s", net));
                    return sca;
                }));

        final var toCreate = new CreateSmartContractRequest();
        toCreate.setName("test" + sequence);
        toCreate.setVaultId(vault.getId());
        toCreate.setDisplayName("Test Smart Contract");
        toCreate.setAddresses(addresses);

        final var response = client
                .target(format("%s/blockchain/omni/smart_contract", apiRoot))
                .request()
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .post(Entity.entity(toCreate, APPLICATION_JSON));

        assertEquals(response.getStatus(), 200);

        final var created = response.readEntity(SmartContract.class);
        assertNotNull(created.getId());
        assertNull(created.getMetadata());
        assertEquals(created.getDisplayName(), toCreate.getDisplayName());
        assertEquals(created.getAddresses(), toCreate.getAddresses());
        assertEquals(created.getName(), toCreate.getName());

        final var existing = contracts.put(created.getId(), created);
        assertNull(existing);

    }

    @Test(groups = "update", dependsOnGroups = "create", dataProvider = "contractsById")
    public void updateSmartContract(final String contractId, final SmartContract smartContract) {

        final var addresses = Arrays.stream(BlockchainNetwork.values())
                .collect(toMap(net -> net, net -> {
                    final var sca = new SmartContractAddress();
                    sca.setAddress(format("Updated Address for %s", net));
                    return sca;
                }));

        final var toUpdate = new UpdateSmartContractRequest();
        toUpdate.setName(smartContract.getName());
        toUpdate.setVaultId(vault.getId());
        toUpdate.setDisplayName("Test Smart Contract");
        toUpdate.setAddresses(addresses);

        final var response = client
                .target(format("%s/blockchain/omni/smart_contract/%s", apiRoot, contractId))
                .request()
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .put(Entity.entity(toUpdate, APPLICATION_JSON));

        assertEquals(response.getStatus(), 200);

        final var updated = response.readEntity(SmartContract.class);
        assertNotNull(updated.getId());
        assertNull(updated.getMetadata());
        assertEquals(updated.getDisplayName(), updated.getDisplayName());
        assertEquals(updated.getAddresses(), updated.getAddresses());
        assertEquals(updated.getName(), updated.getName());

        final var existing = contracts.put(updated.getId(), updated);
        assertEquals(existing, smartContract);

    }

    @Test(groups = "read", dependsOnGroups = "update")
    public void testGetSmartContracts() {

        final var contracts = new PaginationWalker().toList((offset, count) -> client
                .target(format("%s/blockchain/omni/smart_contract", apiRoot))
                .request()
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .get(SmartContractPagination.class)
        );

        assertTrue(contracts.size() > 0);
        contracts.forEach(contract -> assertTrue(this.contracts.containsKey(contract.getId())));

    }

    @Test(groups = "read", dependsOnGroups = "update", dataProvider = "contractsById")
    public void testGetSmartContract(final String contractId, final SmartContract smartContract) {

        final var response = client
                .target(format("%s/blockchain/omni/smart_contract/%s", apiRoot, contractId))
                .request()
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .get();

        assertEquals(response.getStatus(), 200);

        final var fetched = response.readEntity(SmartContract.class);
        assertEquals(fetched, smartContract);

    }

    @Test(groups = "delete", dependsOnGroups = "read", dataProvider = "contractsById")
    public void testDeleteSmartContract(final String contractId, final SmartContract smartContract) {

        final var response = client
                .target(format("%s/blockchain/omni/smart_contract/%s", apiRoot, contractId))
                .request()
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .delete();

        assertEquals(response.getStatus(), 204);

    }

    @Test(groups = "delete", dependsOnGroups = "read", dependsOnMethods = "testDeleteSmartContract", dataProvider = "contractsById")
    public void testDoubleDeleteSmartContract(final String contractId, final SmartContract smartContract) {

        final var response = client
                .target(format("%s/blockchain/omni/smart_contract/%s", apiRoot, contractId))
                .request()
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .delete();

        assertEquals(response.getStatus(), 404);

    }

    @Test(groups = "delete", dependsOnGroups = "read", dependsOnMethods = "testDeleteSmartContract", dataProvider = "contractsById")
    public void testDeletedContractsDoNoExist(final String contractId, final SmartContract smartContract) {

        final var response = client
                .target(format("%s/blockchain/omni/smart_contract/%s", apiRoot, contractId))
                .request()
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .get();

        assertEquals(response.getStatus(), 404);

    }


}
