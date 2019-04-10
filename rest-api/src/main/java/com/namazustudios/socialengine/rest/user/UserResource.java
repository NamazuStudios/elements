package com.namazustudios.socialengine.rest.user;

import com.google.common.base.Strings;
import com.namazustudios.socialengine.model.UserCreateRequest;
import com.namazustudios.socialengine.model.ValidationGroups;
import com.namazustudios.socialengine.model.ValidationGroups.Create;
import com.namazustudios.socialengine.util.ValidationHelper;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.InvalidParameterException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Objects;

import static com.google.common.base.Strings.nullToEmpty;
import static com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource.SESSION_SECRET;

/**
 * Created by patricktwohig on 3/25/15.
 */
@Api(value = "Users",
     description = "Manages users in the server.  Users are single-end users typically associated " +
                   "with a login name or email address.",
     authorizations = {@Authorization(SESSION_SECRET)})
@Path("user")
public class UserResource {

    private UserService userService;

    private ValidationHelper validationHelper;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Search Users",
                  notes = "Searches all users in the system and returning the metadata for all matches against " +
                          "the given search filter.")
    public Pagination<User> getUsers(
            @QueryParam("offset") @DefaultValue("0") final int offset,
            @QueryParam("count")  @DefaultValue("20") final int count,
            @QueryParam("search") final String search) {

        if (offset < 0) {
            throw new InvalidParameterException("Offset must have positive value.");
        }

        if (count < 0) {
            throw new InvalidParameterException("Count must have positive value.");
        }

        final String query = nullToEmpty(search).trim();

        return query.isEmpty() ?
            getUserService().getUsers(offset, count) :
            getUserService().getUsers(offset, count, search);

    }

    @GET
    @Path("{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets a Specific User",
                  notes = "Gets a specific user by email or unique user ID.")
    public User getUser(@PathParam("name") final String name) {
        return getUserService().getUser(name);
    }

    @GET
    @Path("me")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets the current User",
                  notes = "A special endpoint used to get the current user for the request.  The current " +
                          "user is typically associated with the session but may be derived any other way.  This " +
                          "is essentially an alias for using GET /user/myUserId")
    public User getUser() {
        return getUserService().getCurrentUser();
    }

    @PUT
    @Path("{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Updates a User",
                  notes = "Supplying the user object, this will update the user with the new information supplied " +
                          "in the body of the request.  Optionally, the user's password may be provided in the User " +
                          "object.")
    public User updateUser(final User user,
                           @PathParam("name") String name) {

        getValidationHelper().validateModel(user);

        // Manually check if id is present: we should not include @NotNull in the model as it may interfere with e.g.
        // user creation where id is not possible to be provided.
        if (user.getId() == null) {
            throw new InvalidDataException("id must be provided in the User object.");
        }

        name = Strings.nullToEmpty(name).trim();

        String password = user.getPassword();
        password = Strings.nullToEmpty(password).trim();

        if (user.getName() == null) {
            user.setName(name);
        }

        if (Strings.isNullOrEmpty(name)) {
            throw new NotFoundException("User not found.");
        }

        if (Strings.isNullOrEmpty(password)) {
            return getUserService().updateUser(user);
        } else {
            return getUserService().updateUser(user, password);
        }

    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Creates a User",
                  notes = "Supplying the user object, this will update the user with the new information supplied " +
                          "in the body of the request.  Optionally, the user's password may be provided in the User " +
                          "object.")
    public User createUser(final User user) {

        getValidationHelper().validateModel(user);

        String password = user.getPassword();
        password = Strings.nullToEmpty(password).trim();

        if (password.isEmpty()){
            return getUserService().createUser(user);
        } else {
            return getUserService().createUser(user, password);
        }

    }

    @DELETE
    @Path("{name}")
    @ApiOperation(value = "Deletes a User",
                  notes = "Deletes and permanently removes the user from the server.  The server may keep " +
                          "some metadata as necessary to avoid data inconsistency.  However, the user has been " +
                          "deleted from the client standpoint and will not be accessible through any of the existing " +
                          "APIs.")
    public void deactivateUser(@PathParam("name") final String name) {
        userService.deleteUser(name);
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
