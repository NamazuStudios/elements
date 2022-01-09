package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.BlockchainConstants;
import com.namazustudios.socialengine.model.blockchain.PatchSmartContractRequest;
import com.namazustudios.socialengine.model.session.SessionCreation;
import com.namazustudios.socialengine.model.user.User;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;

import static com.namazustudios.socialengine.Headers.SESSION_SECRET;
import static com.namazustudios.socialengine.Headers.SOCIALENGINE_SESSION_SECRET;
import static com.namazustudios.socialengine.rest.TestUtils.TEST_API_ROOT;
import static java.util.UUID.randomUUID;

/*
Test Neo contract CRUD operations
Test contract function invocations
 */
public class NeoContractTest {

    @Factory
    public Object[] getTests() {
        return new Object[] {
                TestUtils.getInstance().getXodusTest(NeoContractTest.class),
                TestUtils.getInstance().getUnixFSTest(NeoContractTest.class)
        };
    }

    private User user;

    private SessionCreation sessionCreation;

    private final String name = "testuser-name-" + randomUUID().toString();

    private final String email = "testuser-email-" + randomUUID().toString() + "@example.com";

    private final String password = randomUUID().toString();

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private Client client;

    @Inject
    private ClientContext superUserClientContext;

    @DataProvider
    public Object[][] getAuthHeader() {
        return new Object[][] {
                new Object[] { SESSION_SECRET },
                new Object[] { SOCIALENGINE_SESSION_SECRET }
        };
    }

    @Test
    public void createUser() {
        superUserClientContext
                .createSuperuser("tokenAdmin")
                .createSession();
    }

    @DataProvider
    public Object[][] credentialsProvider() {
        return new Object[][] {
                new Object[]{name, password},
                new Object[]{email, password},
                new Object[]{user.getId(), password}
        };
    }

    @Test
    public void CreateContractExpectingFailureNoAuth() {
        final PatchSmartContractRequest neoSmartContractRequest = new PatchSmartContractRequest();

        neoSmartContractRequest.setBlockchain(BlockchainConstants.Names.NEO);
        
    }
}
