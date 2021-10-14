package com.namazustudios.socialengine.rest;

import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;

import static com.namazustudios.socialengine.rest.TestUtils.TEST_API_ROOT;

public class InventoryApiTest {


    @Factory
    public static Object[] getTests() {
        return new Object[] {
            TestUtils.getInstance().getXodusTest(InventoryApiTest.class),
            TestUtils.getInstance().getUnixFSTest(InventoryApiTest.class)
        };
    }

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private Client client;

    @Inject
    private ClientContext user0;

    @Inject
    private ClientContext user1;

    @Inject
    private ClientContext superUser;

    @Test
    public void createUsers() {
        user0.createUser("user0").createSession();
        user1.createUser("user1").createSession();
        superUser.createSuperuser("admin").createSession();
    }

    @Test(dependsOnMethods = "createUsers")
    public void createCreatUserInventory(final ClientContext userContext) {

    }

}
