package dev.getelements.elements.rest;

import org.testng.annotations.Factory;

import javax.inject.Inject;

public class UpdateUserAvatarTest {

    @Inject
    private ClientContext userClientContext;

    @Factory
    public Object[] getTests() {
        return new Object[] {
                TestUtils.getInstance().getXodusTest(UpdateUserAvatarTest.class),
                TestUtils.getInstance().getUnixFSTest(UpdateUserAvatarTest.class)
        };
    }

    private void setUp() {
        userClientContext.createUser("vaultboy").createSession();

//        vault = createVault(userClientContext);
//        emptyVault = createVault(userClientContext);
    }
}
