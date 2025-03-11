package dev.getelements.elements.rest.test;

import dev.getelements.elements.sdk.model.largeobject.LargeObject;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.profile.UpdateProfileImageRequest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import java.io.InputStream;

import static dev.getelements.elements.sdk.model.Headers.SESSION_SECRET;
import static dev.getelements.elements.rest.test.TestUtils.TEST_API_ROOT;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class UpdateUserAvatarTest {

    @Inject
    private ClientContext clientContext;

    @Inject
    private Client client;

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;

    @Factory
    public Object[] getTests() {
        return new Object[] {
                TestUtils.getInstance().getTestFixture(UpdateUserAvatarTest.class)
        };
    }

    @BeforeClass
    private void setUp() {
        clientContext.createUser("user");
        clientContext.createProfile("Test Display");
    }

    @Test
    public void shouldUpdateUserAvatar() {

        clientContext.createSession(clientContext.getDefaultProfile());

        String profileId = clientContext.getDefaultProfile().getId();
        String largeObjectIdBeforeUpdate = clientContext.getDefaultProfile().getImageObject().getId();

//        updateLargeObjectContent(clientContext.getDefaultProfile().getImageObject().getId());

        UpdateProfileImageRequest updateProfileImageRequest = new UpdateProfileImageRequest();
        updateProfileImageRequest.setMimeType("image/jpeg");

        Profile updatedProfile = client
                .target(apiRoot + "/profile/" + profileId + "/image")
                .request()
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .put(Entity.entity(updateProfileImageRequest, MediaType.APPLICATION_JSON_TYPE))
                .readEntity(Profile.class);

        assertNotNull(updatedProfile.getImageObject().getId());
        assertEquals(updatedProfile.getImageObject().getId(), largeObjectIdBeforeUpdate);
        assertEquals(updatedProfile.getImageObject().getMimeType(), "image/jpeg");
    }

    private void updateLargeObjectContent(String largeObjectId) {
        final InputStream loStream = UserLargeObjectResourceTest.class.getResourceAsStream("/testLO.txt");

        client
                .target(apiRoot + "/large_object/" + largeObjectId + "/content")
                .request()
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .put(Entity.entity(loStream, MediaType.APPLICATION_JSON_TYPE))
                .readEntity(LargeObject.class);
    }

}
