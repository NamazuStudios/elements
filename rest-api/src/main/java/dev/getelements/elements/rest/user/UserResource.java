package dev.getelements.elements.rest.user;

import dev.getelements.elements.exception.InvalidParameterException;
import dev.getelements.elements.exception.NotFoundException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.session.SessionCreation;
import dev.getelements.elements.model.user.*;
import dev.getelements.elements.rest.AuthSchemes;
import dev.getelements.elements.service.UserService;
import dev.getelements.elements.util.ValidationHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Strings.nullToEmpty;

/**
 * Created by patricktwohig on 3/25/15.
 */
@Api(value = "Users",
     description = "Manages users in the server.  Users are single-end users typically associated " +
                   "with a login name or email address.",
     authorizations = {@Authorization(AuthSchemes.AUTH_BEARER), @Authorization(AuthSchemes.SESSION_SECRET), @Authorization(AuthSchemes.SOCIALENGINE_SESSION_SECRET)})
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
    public User getCurrentUser() {
        return getUserService().getCurrentUser();
    }

    @PUT
    @Path("{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Updates a User",
                  notes = "Supplying the user object, this will update the user with the new information supplied " +
                          "in the body of the request.  Optionally, the user's password may be provided in the User " +
                          "object.")
    public User updateUser(final UserUpdateRequest userUpdateRequest,
                           @PathParam("userId") String userId) {

        getValidationHelper().validateModel(userUpdateRequest);

        if (isNullOrEmpty(userId)) {
            throw new NotFoundException("User not found.");
        }

        return getUserService().updateUser(userId, userUpdateRequest);

    }

    @PUT
    @Path("{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Updates a User",
            notes = "Supplying the user object, this will update the user with the new information supplied " +
                    "in the body of the request.  Optionally, the user's password may be provided in the User " +
                    "object.")
    public SessionCreation updateUserPassword(final UserUpdatePasswordRequest userUpdatePasswordRequest,
                                              final @PathParam("userId") String userId) {

        getValidationHelper().validateModel(userUpdatePasswordRequest);

        if (isNullOrEmpty(userId)) {
            throw new NotFoundException("User not found.");
        }

        return getUserService().updateUserPassword(userId, userUpdatePasswordRequest);

    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Creates a User",
                  notes = "Supplying the user object, this will update the user with the new information supplied " +
                          "in the body of the request.  Optionally, the user's password may be provided in the User " +
                          "object.")
    public UserCreateResponse createUser(final UserCreateRequest userCreateRequest) {
        getValidationHelper().validateModel(userCreateRequest);
        return getUserService().createUser(userCreateRequest);
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
