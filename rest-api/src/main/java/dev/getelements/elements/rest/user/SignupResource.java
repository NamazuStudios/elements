package dev.getelements.elements.rest.user;

import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.session.UsernamePasswordSessionRequest;
import dev.getelements.elements.sdk.model.user.UserCreateRequest;
import dev.getelements.elements.sdk.model.user.UserCreateResponse;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.getelements.elements.sdk.service.auth.UsernamePasswordAuthService;
import dev.getelements.elements.sdk.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("signup")
public class SignupResource {

    private UserService userService;

    private UsernamePasswordAuthService authService;

    private ValidationHelper validationHelper;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Sign Up a User",
            description = "Supplying the UserCreateRequest object, this will create a " +
                    "new user and create a session for it.  If any Profiles are supplied, the " +
                    "first one will be selected for the session creation.")
    @Path("session")
    public SessionCreation signUpUserAndCreateSession(final UserCreateRequest userCreateRequest) {

        getValidationHelper().validateModel(userCreateRequest, ValidationGroups.Create.class);

        final var userCreateResponse = getUserService().createUser(userCreateRequest);
        final var sessionRequest = new UsernamePasswordSessionRequest();

        sessionRequest.setUserId(userCreateResponse.getId());
        sessionRequest.setPassword(userCreateRequest.getPassword());

        if(userCreateResponse.getProfiles() != null && !userCreateResponse.getProfiles().isEmpty()) {
            sessionRequest.setProfileId(userCreateResponse.getProfiles().getFirst().getId());
        }

        return getAuthService().createSession(sessionRequest);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Sign Up a User",
            description = "Supplying the user create request object, this will create a new user.")
    @Deprecated
    public UserCreateResponse signUpUser(final UserCreateRequest userCreateRequest) {
        getValidationHelper().validateModel(userCreateRequest, ValidationGroups.Create.class);
        return getUserService().createUser(userCreateRequest);
    }

    public UserService getUserService() {
        return userService;
    }

    @Inject
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public UsernamePasswordAuthService getAuthService() {
        return authService;
    }

    @Inject
    public void setAuthService(UsernamePasswordAuthService authService) {
        this.authService = authService;
    }
}
