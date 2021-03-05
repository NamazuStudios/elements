package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.model.Version;
import com.namazustudios.socialengine.service.VersionService;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;

import static com.namazustudios.socialengine.rest.TestUtils.*;
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
