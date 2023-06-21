package dev.getelements.elements.rest;

import dev.getelements.elements.model.Version;
import dev.getelements.elements.service.VersionService;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;

import static dev.getelements.elements.rest.TestUtils.*;
import static org.testng.Assert.assertEquals;

public class AnonymousUserTest {

    @Factory
    public Object[] getTests() {
        return new Object[] {
            TestUtils.getInstance().getXodusTest(AnonymousUserTest.class),
            TestUtils.getInstance().getUnixFSTest(AnonymousUserTest.class)
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
