package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.model.profile.CreateProfileRequest;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.model.profile.UpdateProfileRequest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.namazustudios.socialengine.Headers.SESSION_SECRET;
import static com.namazustudios.socialengine.Headers.SOCIALENGINE_SESSION_SECRET;
import static com.namazustudios.socialengine.rest.TestUtils.TEST_API_ROOT;
import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class SuperuserCreateUserAndProfileTest {

    @Factory
    public Object[] getTests() {
        return new Object[] {
            TestUtils.getInstance().getXodusTest(SuperuserCreateUserAndProfileTest.class),
            TestUtils.getInstance().getUnixFSTest(SuperuserCreateUserAndProfileTest.class)
        };
    }

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private Client client;

    @Inject
    private ClientContext userClientContext;

    @Inject
    private ClientContext superUserClientContext;

    private Profile profile;

    @DataProvider
    public Object[][] getAuthHeader() {
        return new Object[][] {
            new Object[] { SESSION_SECRET },
            new Object[] { SOCIALENGINE_SESSION_SECRET }
        };
    }

    @Test
    public void createUser() {

        userClientContext
            .createUser("user")
            .createSession();

        superUserClientContext
            .createSuperuser("admin")
            .createSession();

    }

    @Test(dependsOnMethods = "createUser", dataProvider = "getAuthHeader")
    public void createProfile(final String authHeader) {

        final var toCreate = new CreateProfileRequest();

        final var metadata = new HashMap<String, Object>();

        metadata.put("foo0", "bar0");
        metadata.put("foo1", "bar1");
        metadata.put("fooa", Arrays.asList("bar0, bar1"));
        metadata.put("fooo", Map.of(
            "a", "b",
            "c", "d",
            "e", "f",
            "g", "h",
            "h", "i",
            "j", "k"
        ) );

        toCreate.setDisplayName("Paddy O' Furniture");
        toCreate.setImageUrl("http://example.com/image.jpg");
        toCreate.setUserId(userClientContext.getUser().getId());
        toCreate.setApplicationId(superUserClientContext.getApplication().getId());
        toCreate.setMetadata(metadata);

        profile = client
            .target(apiRoot + "/profile")
            .request()
            .header(authHeader, superUserClientContext.getSessionSecret())
            .post(Entity.entity(toCreate, APPLICATION_JSON))
            .readEntity(Profile.class);

        assertNotNull(profile.getId());

        assertEquals(profile.getMetadata(), metadata);
        assertEquals(profile.getDisplayName(), toCreate.getDisplayName());
        assertEquals(profile.getImageUrl(), toCreate.getImageUrl());

        assertEquals(profile.getUser().getId(), userClientContext.getUser().getId());
        assertEquals(profile.getApplication().getId(), userClientContext.getApplication().getId());

    }

    @Test(dependsOnMethods = "createProfile", dataProvider = "getAuthHeader")
    public void updateProfile(final String authHeader) {

        final var toUpdate = new UpdateProfileRequest();

        final var metadata = new HashMap<String, Object>();

        metadata.put("foo1", "bar1");
        metadata.put("foo2", "bar2");
        metadata.put("foo3", Arrays.asList("bar1, bar2"));
        metadata.put("fooo", Map.of(
                "a0", "b0",
                "c0", "d0",
                "e0", "f0",
                "g0", "h0",
                "h0", "i0",
                "j0", "k0"
        ) );

        toUpdate.setDisplayName("Paddy O' Furniture 1");
        toUpdate.setImageUrl("http://example.com/image1.jpg");
        toUpdate.setMetadata(metadata);

        profile = client
            .target(format("%s/profile/%s", apiRoot, profile.getId()))
            .request()
            .header(authHeader, superUserClientContext.getSessionSecret())
            .put(Entity.entity(toUpdate, APPLICATION_JSON))
            .readEntity(Profile.class);

        assertNotNull(profile.getId());

        assertEquals(profile.getMetadata(), metadata);
        assertEquals(profile.getDisplayName(), toUpdate.getDisplayName());
        assertEquals(profile.getImageUrl(), toUpdate.getImageUrl());

        assertEquals(profile.getUser().getId(), userClientContext.getUser().getId());
        assertEquals(profile.getApplication().getId(), userClientContext.getApplication().getId());

    }



    @Test(dependsOnMethods = "updateProfile", dataProvider = "getAuthHeader")
    public void createProfileRegression(final String authHeader) {

        final var toCreate = new Profile();

        final var metadata = new HashMap<String, Object>();

        metadata.put("foo0", "bar0");
        metadata.put("foo1", "bar1");
        metadata.put("fooa", Arrays.asList("bar0, bar1"));
        metadata.put("fooo", Map.of(
                "a", "b",
                "c", "d",
                "e", "f",
                "g", "h",
                "h", "i",
                "j", "k"
        ) );

        toCreate.setDisplayName("Paddy O' Furniture");
        toCreate.setImageUrl("http://example.com/image.jpg");
        toCreate.setUser(userClientContext.getUser());
        toCreate.setApplication(superUserClientContext.getApplication());
        toCreate.setMetadata(metadata);

        profile = client
            .target(apiRoot + "/profile")
            .request()
            .header(authHeader, superUserClientContext.getSessionSecret())
            .post(Entity.entity(toCreate, APPLICATION_JSON))
            .readEntity(Profile.class);

        assertNotNull(profile.getId());

        assertEquals(profile.getMetadata(), metadata);
        assertEquals(profile.getDisplayName(), toCreate.getDisplayName());
        assertEquals(profile.getImageUrl(), toCreate.getImageUrl());

        assertEquals(profile.getUser().getId(), userClientContext.getUser().getId());
        assertEquals(profile.getApplication().getId(), userClientContext.getApplication().getId());

    }

    @Test(dependsOnMethods = "createProfileRegression", dataProvider = "getAuthHeader")
    public void updateProfileRegression(final String authHeader) {

        final var toUpdate = new Profile();

        final var metadata = new HashMap<String, Object>();

        metadata.put("foo1", "bar1");
        metadata.put("foo2", "bar2");
        metadata.put("foo3", Arrays.asList("bar1, bar2"));
        metadata.put("fooo", Map.of(
                "a0", "b0",
                "c0", "d0",
                "e0", "f0",
                "g0", "h0",
                "h0", "i0",
                "j0", "k0"
        ) );

        toUpdate.setDisplayName("Paddy O' Furniture 1");
        toUpdate.setImageUrl("http://example.com/image1.jpg");
        toUpdate.setMetadata(metadata);

        profile = client
                .target(format("%s/profile/%s", apiRoot, profile.getId()))
                .request()
                .header(authHeader, superUserClientContext.getSessionSecret())
                .put(Entity.entity(toUpdate, APPLICATION_JSON))
                .readEntity(Profile.class);

        assertNotNull(profile.getId());

        assertEquals(profile.getMetadata(), metadata);
        assertEquals(profile.getDisplayName(), toUpdate.getDisplayName());
        assertEquals(profile.getImageUrl(), toUpdate.getImageUrl());

        assertEquals(profile.getUser().getId(), userClientContext.getUser().getId());
        assertEquals(profile.getApplication().getId(), userClientContext.getApplication().getId());

    }

}
