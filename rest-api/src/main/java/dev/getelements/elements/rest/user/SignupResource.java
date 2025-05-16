package dev.getelements.elements.rest.user;

import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.user.UserCreateRequest;
import dev.getelements.elements.sdk.model.user.UserCreateResponse;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
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

    private ValidationHelper validationHelper;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Sign Up a User",
            description = "Supplying the user create request object, this will create a new user.")
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

}
