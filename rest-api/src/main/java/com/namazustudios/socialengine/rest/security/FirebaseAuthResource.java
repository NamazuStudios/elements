package com.namazustudios.socialengine.rest.security;

import com.namazustudios.socialengine.model.session.FirebaseSessionRequest;
import com.namazustudios.socialengine.model.session.SessionCreation;
import com.namazustudios.socialengine.service.FirebaseAuthService;
import com.namazustudios.socialengine.util.ValidationHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import static com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource.*;

/**
 * Created by patricktwohig on 4/2/15.
 */
@Api(value = "FirebaseInSession",
     authorizations = { @Authorization(AUTH_BEARER), @Authorization(SESSION_SECRET), @Authorization(SOCIALENGINE_SESSION_SECRET) }
 )
@Path("firebase_session")
public class FirebaseAuthResource {

    private ValidationHelper validationHelper;

    private FirebaseAuthService firebaseAuthService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Creates a Session using Firebase",
            notes = "Begins a session using Firebase login. Unlike other auth methods, which require the client " +
                    "specify the profile and name, this uses the supplied JWT token. Embedded in the JWT is the " +
                    "identifier for the application. Therefore, Elements will use Firebase's server-to-server APIs " +
                    "in order determine the application configuration to use. Just like other session APIs, if the " +
                    "session specifies a user, then this will link the existing account to the supplied firebase ID.")
    public SessionCreation createFirebaseSession(final FirebaseSessionRequest firebaseSessionRequest) {
        getValidationHelper().validateModel(firebaseSessionRequest);
        return getFirebaseAuthService().createOrUpdateUserWithFirebaseJWT(firebaseSessionRequest);
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public FirebaseAuthService getFirebaseAuthService() {
        return firebaseAuthService;
    }

    @Inject
    public void setFirebaseAuthService(FirebaseAuthService firebaseAuthService) {
        this.firebaseAuthService = firebaseAuthService;
    }

}
