package dev.getelements.elements.rest;

import dev.getelements.elements.model.follower.CreateFollowerRequest;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.rest.model.ProfilePagination;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import static dev.getelements.elements.rest.TestUtils.TEST_API_ROOT;
import static java.lang.String.format;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

public class UserFollowerApiTest {

    @Factory
    public Object[] getTests() {
        return new Object[] {
                TestUtils.getInstance().getXodusTest(UserFollowerApiTest.class),
                TestUtils.getInstance().getUnixFSTest(UserFollowerApiTest.class)
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


    @DataProvider
    public Object[][] getClientContexts() {
        return new Object[][] {
                {user0, user1},
                {user1, user0}
        };
    }

    @BeforeClass
    public void testSetup() {

        user0.createUser("follower_user_0")
                .createProfile("follower_user_0")
                .createSession();

        user1.createUser("follower_user_1")
                .createProfile("follower_user_1")
                .createSession();

    }

    @Test(groups = "create", dataProvider = "getClientContexts")
    public void testFollowOtherUser(final ClientContext follower, final ClientContext followee) {

        final var request = new CreateFollowerRequest();
        request.setFollowedId(followee.getDefaultProfile().getId());

        final var response = client
                .target(format("%s/follower/%s", apiRoot, follower.getDefaultProfile().getId()))
                .request()
                .header("Authorization", format("Bearer %s", follower.getSessionSecret()))
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        assertEquals(204, response.getStatus());

    }

    @Test(groups = "fetch", dataProvider = "getClientContexts", dependsOnGroups = "create")
    public void testGetFollower(final ClientContext follower, final ClientContext followee) {

        final var response = client
                .target(format("%s/follower/%s/%s",
                        apiRoot,
                        follower.getDefaultProfile().getId(),
                        followee.getDefaultProfile().getId())
                )
                .request()
                .header("Authorization", format("Bearer %s", follower.getSessionSecret()))
                .get();

        assertEquals(200, response.getStatus());

        final var fetched = response.readEntity(Profile.class);
        assertEquals(fetched.getId(), followee.getDefaultProfile().getId());
        assertNotNull(fetched.getImageObject().getUrl());

    }

    @Test(groups = "fetch", dataProvider = "getClientContexts", dependsOnGroups = "create")
    public void testGetFollowers(final ClientContext follower, final ClientContext followee) {

        final var response = client
                .target(format("%s/follower/%s", apiRoot, follower.getDefaultProfile().getId()))
                .request()
                .header("Authorization", format("Bearer %s", follower.getSessionSecret()))
                .get();

        assertEquals(200, response.getStatus());

        final var pagination = response.readEntity(ProfilePagination.class);

        final var profile = pagination
                .getObjects()
                .stream()
                .filter(p -> p.getId().equals(followee.getDefaultProfile().getId()))
                .findAny()
                .orElse(null);

        assertNotNull(profile);
        assertNotNull(profile.getImageObject().getUrl());
    }

    @Test(groups = "fetch", dataProvider = "getClientContexts", dependsOnGroups = "create")
    public void testGetFollowees(final ClientContext follower, final ClientContext followee) {

        final var response = client
                .target(format("%s/followee/%s", apiRoot, followee.getDefaultProfile().getId()))
                .request()
                .header("Authorization", format("Bearer %s", follower.getSessionSecret()))
                .get();

        assertEquals(200, response.getStatus());

        final var pagination = response.readEntity(ProfilePagination.class);

        final var profile = pagination
                .getObjects()
                .stream()
                .filter(p -> p.getId().equals(follower.getDefaultProfile().getId()))
                .findAny()
                .orElse(null);

        assertNotNull(profile);
        assertNotNull(profile.getImageObject().getUrl());
    }

    @Test(groups = "delete", dataProvider = "getClientContexts", dependsOnGroups = "fetch")
    public void testDeleteFollower(final ClientContext follower, final ClientContext followee) {

        final var response = client
                .target(format("%s/follower/%s/%s",
                        apiRoot,
                        follower.getDefaultProfile().getId(),
                        followee.getDefaultProfile().getId())
                )
                .request()
                .header("Authorization", format("Bearer %s", follower.getSessionSecret()))
                .delete();

        assertEquals(204, response.getStatus());

    }

    @Test(groups = "delete", dataProvider = "getClientContexts", dependsOnGroups = "fetch")
    public void testFollowerDoubleDelete(final ClientContext follower, final ClientContext followee) throws InterruptedException {

        final var response = client
                .target(format("%s/follower/%s/%s",
                        apiRoot,
                        follower.getDefaultProfile().getId(),
                        followee.getDefaultProfile().getId())
                )
                .request()
                .header("Authorization", format("Bearer %s", follower.getSessionSecret()))
                .delete();

        assertEquals(404, response.getStatus());

    }

    @Test(groups = "delete", dataProvider = "getClientContexts", dependsOnGroups = "fetch")
    public void testEnsureFollowerDelete(final ClientContext follower, final ClientContext followee) {

        final var response = client
                .target(format("%s/follower/%s/%s",
                        apiRoot,
                        follower.getDefaultProfile().getId(),
                        followee.getDefaultProfile().getId())
                )
                .request()
                .header("Authorization", format("Bearer %s", follower.getSessionSecret()))
                .get();

        assertEquals(404, response.getStatus());

    }

}
