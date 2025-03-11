package dev.getelements.elements.rest.test;

import dev.getelements.elements.sdk.model.Version;
import dev.getelements.elements.sdk.service.version.VersionService;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import static dev.getelements.elements.rest.test.TestUtils.*;
import static org.testng.Assert.assertEquals;

public class AnonymousUserTest {

    @Factory
    public Object[] getTests() {
        return new Object[] {
            getInstance().getTestFixture(AnonymousUserTest.class)
        };
    }

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;
    
    @Inject
    private Client client;

    @Inject
    @Named(TEST_INSTANCE)
    private VersionService versionService;

    @Test
    public void testGetVersion() throws Exception {

        final Version version = client
                .target(apiRoot + "/version")
                .queryParam("count", 20)
                .request()
                .buildGet()
                .submit(Version.class)
                .get();

        assertEquals(version, versionService.getVersion());

    }

}
