package dev.getelements.elements.rest.user;

import dev.getelements.elements.model.ValidationGroups;
import dev.getelements.elements.model.user.UserCreateRequest;
import dev.getelements.elements.model.user.UserCreateResponse;
import dev.getelements.elements.service.UserService;
import dev.getelements.elements.util.ValidationHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Api(value = "User Signup",
     description = "Manages users in the server.  Users are single-end users typically associated " +
                   "with a login name or email address.")
@Path("signup")
public class SignupResource {

    private UserService userService;

    private ValidationHelper validationHelper;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Sign Up a User",
            notes = "Supplying the user create request object, this will create a new user.")
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
