package dev.getelements.elements.rest.user;

import dev.getelements.elements.sdk.model.exception.InvalidParameterException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.exception.user.UserNotFoundException;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.user.*;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.getelements.elements.sdk.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Strings.nullToEmpty;

/**
 * Created by patricktwohig on 3/25/15.
 */
@Path("user")
public class UserResource {

    private UserService userService;

    private ValidationHelper validationHelper;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Search Users",
            description = "Searches all users in the system and returning the metadata for all matches against " +
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
    @Operation(
            summary = "Gets a Specific User",
            description = "Gets a specific user by name, email, or unique user ID.")
    public User getUser(@PathParam("name") final String name) {

        final var user = getUserService().getUser(name);

        if (user == null) {
            throw new UserNotFoundException("User not found.");
        }

        return user;
    }

    @GET
    @Path("me")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Gets the current User",
            description = "A special endpoint used to get the current user for the request.  The current " +
                          "user is typically associated with the session but may be derived any other way.  This " +
                          "is essentially an alias for using GET /user/myUserId")
    public User getCurrentUser() {
        return getUserService().getCurrentUser();
    }

    @PUT
    @Path("{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Updates a User",
            description = "Supplying the user object, this will update the user with the new information supplied " +
                          "in the body of the request.  Optionally, the user's password may be provided in the User " +
                          "object.")
    public User updateUser(final UserUpdateRequest userUpdateRequest,
                           @PathParam("userId") String userId) {

        getValidationHelper().validateModel(userUpdateRequest);

        if (isNullOrEmpty(userId)) {
            throw new UserNotFoundException("User not found.");
        }

        return getUserService().updateUser(userId, userUpdateRequest);

    }

    @PUT
    @Path("{userId}/password")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Updates a User's Password",
            description = "Supplying the UserUpdatePasswordRequest, this will attempt to update the user's password only " +
                    "if they supply the correct existing password.")
    public SessionCreation updateUserPassword(final UserUpdatePasswordRequest userUpdatePasswordRequest,
                                              final @PathParam("userId") String userId) {

        getValidationHelper().validateModel(userUpdatePasswordRequest);

        if (isNullOrEmpty(userId)) {
            throw new UserNotFoundException("User not found.");
        }

        return getUserService().updateUserPassword(userId, userUpdatePasswordRequest);

    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Creates a User",
            description = "Supplying the user object, this will update the user with the new information supplied " +
                          "in the body of the request.  Optionally, the user's password may be provided in the User " +
                          "object.")
    public UserCreateResponse createUser(final UserCreateRequest userCreateRequest) {
        getValidationHelper().validateModel(userCreateRequest);
        return getUserService().createUser(userCreateRequest);
    }

    @DELETE
    @Path("{name}")
    @Operation(
            summary = "Deletes a User",
            description = "Deletes and permanently removes the user from the server.  The server may keep " +
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
