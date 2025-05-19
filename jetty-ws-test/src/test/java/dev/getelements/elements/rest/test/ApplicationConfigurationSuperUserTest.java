package dev.getelements.elements.rest.test;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;

import static dev.getelements.elements.rest.test.TestUtils.TEST_API_ROOT;

public class ApplicationConfigurationSuperUserTest {

    @Factory
    public static Object[] getTests() {
        return new Object[] {
                TestUtils.getInstance().getTestFixture(ApplicationConfigurationSuperUserTest.class)
        };
    }

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private Client client;

    @Inject
    private ClientContext user;

    @Inject
    private ClientContext superUser;

    @BeforeClass
    public void setupAdminUser() {
        superUser.createSuperuser("configuration_user").createSession();
        superUser.createSuperuser("configuration_admin").createSession();
    }

}
