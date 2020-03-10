package com.namazustudios.socialengine.rest.user;

import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.UserCreateRequest;
import com.namazustudios.socialengine.model.ValidationGroups;
import com.namazustudios.socialengine.service.UserService;
import com.namazustudios.socialengine.util.ValidationHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import static com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource.SESSION_SECRET;

@Api(value = "User Signup",
     description = "Manages users in the server.  Users are single-end users typically associated " +
                   "with a login name or email address.",
     authorizations = {@Authorization(SESSION_SECRET)})
@Path("signup")
public class SignupResource {

    private UserService userService;

    private ValidationHelper validationHelper;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Sign Up a User",
            notes = "Supplying the user create request object, this will create a new user.")
    public User createUser(final UserCreateRequest userCreateRequest) {
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
