package dev.getelements.elements.rest;

import dev.getelements.elements.model.largeobject.CreateLargeObjectRequest;
import dev.getelements.elements.model.largeobject.LargeObject;
import dev.getelements.elements.model.largeobject.UpdateLargeObjectRequest;
import dev.getelements.elements.model.profile.CreateProfileSignupRequest;
import dev.getelements.elements.model.user.UserCreateRequest;
import dev.getelements.elements.model.user.UserCreateResponse;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import static dev.getelements.elements.Headers.SESSION_SECRET;
import static dev.getelements.elements.rest.TestUtils.TEST_API_ROOT;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

public class UpdateUserAvatarTest {

    @Inject
    private ClientContext clientContext;

    @Inject
    private Client client;

    @Inject
    private LargeObjectRequestFactory requestFactory;

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;
    private final String name = "testuser-" + randomUUID().toString();

    private final String email = "testuser-" + randomUUID().toString() + "@example.com";

    private final String password = randomUUID().toString();
    @Factory
    public Object[] getTests() {
        return new Object[] {
                TestUtils.getInstance().getXodusTest(UpdateUserAvatarTest.class),
                TestUtils.getInstance().getUnixFSTest(UpdateUserAvatarTest.class)
        };
    }

    @BeforeClass
    private void setUp() {
        clientContext.createUser("vaultboy").createSession();

//        vault = createVault(userClientContext);
//        emptyVault = createVault(userClientContext);
    }

    @Test
    public void shouldUpdateUserAvatar() {
        String profileId = createUserWithProfile();
        updateLargeObjectContent();


    }

    private void updateLargeObjectContent() {
        CreateLargeObjectRequest createRequest = requestFactory.createRequestWithAccess(true, true, false);
        UpdateLargeObjectRequest updateRequest = requestFactory.updateLargeObjectRequest(false, false, true);
        updateRequest.setMimeType("changedMime");

        final LargeObject createdLargeObject = client
                .target(apiRoot + "/large_object")
                .request()
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .post(Entity.entity(createRequest, MediaType.APPLICATION_JSON_TYPE))
                .readEntity(LargeObject.class);

        final LargeObject updatedLargeObject = client
                .target(apiRoot + "/large_object/" + createdLargeObject.getId())
                .request()
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .put(Entity.entity(updateRequest, MediaType.APPLICATION_JSON_TYPE))
                .readEntity(LargeObject.class);
    }

    private String createUserWithProfile() {
        final var toCreate = new UserCreateRequest();
        toCreate.setName(name);
        toCreate.setEmail(email);
        toCreate.setPassword(password);

        final var createProfileSignupRequest = new CreateProfileSignupRequest();
        createProfileSignupRequest.setDisplayName("Paddy O' Furniture");
        createProfileSignupRequest.setApplicationId(clientContext.getApplication().getId());
        toCreate.setProfiles(singletonList(createProfileSignupRequest));

        final var response = client
                .target(apiRoot + "/signup")
                .request()
                .post(Entity.entity(toCreate, APPLICATION_JSON))
                .readEntity(UserCreateResponse.class);

        return "profileId";
    }
}
