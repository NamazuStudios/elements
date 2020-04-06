package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.Version;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.service.VersionService;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.ws.rs.client.Client;

import static com.namazustudios.socialengine.Headers.SESSION_SECRET;
import static org.testng.Assert.*;

@Guice(modules = {EmbeddedRestApiIntegrationTestModule.class})
public class AnonymousUserTest {

    @Inject
    private Client client;

    @Inject
    private VersionService versionService;

    @Test
    public void testGetVersion() throws Exception {

        final Version version = client
                .target("http://localhost:8080/api/rest/version")
                .queryParam("count", 20)
                .request()
                .buildGet()
                .submit(Version.class)
                .get();

        assertEquals(version, versionService.getVersion());

    }



}
