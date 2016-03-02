package com.namazustudios.socialengine.rest;

import com.google.common.base.Strings;
import com.namazustudios.socialengine.ValidationHelper;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.InvalidParameterException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.Objects;

/**
 * Created by patricktwohig on 3/25/15.
 */
@Path("user")
@Api(value = "Users",
     description = "Manages users in the server.  Users are single-end users typically associated " +
                   "with a login name or email address.")
public class UserResource {

    @Inject
    private UserService userService;

    @Inject
    private ValidationHelper validationService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{offset}/{count}/{search}")
    @ApiOperation(value = "Search Users",
                  notes = "Searches all users in the system and returning the metadata for all matches against " +
                          "the given search filter.")
    public Pagination<User> searchUsers(
            @PathParam("offset") final int offset,
            @PathParam("count")  final int count,
            @PathParam("search") final String search) {

        if (offset < 0) {
            throw new InvalidParameterException("Offset must have positive value.");
        }

        if (count < 0) {
            throw new InvalidParameterException("Count must have positive value.");
        }

        return userService.getUsers(offset, count, search);

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{offset}/{count}")
    @ApiOperation(value = "Gets Users",
                  notes = "Gets all users known to the server in the default order.")
    public Pagination<User> getUsers(
            @PathParam("offset") final int offset,
            @PathParam("count")  final int count) {

        if (offset < 0) {
            throw new InvalidParameterException("Offset must have positive value.");
        }

        if (count < 0) {
            throw new InvalidParameterException("Count must have positive value.");
        }

        return userService.getUsers(offset, count);

    }


    @GET
    @Path("{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets a Specific User",
                  notes = "Gets a specific user by email or unique user ID.")
    public User getUser(@PathParam("name") final String name) {
        return userService.getUser(name);
    }

    @GET
    @Path("me")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets the current User",
                  notes = "A special endpoint used to get the current user for the request.  The current " +
                          "user is typically associated with the session but may be derived any other way.  This " +
                          "is essentially an alias for using GET /user/myUserId")
    public User getUser() {
        return userService.getCurrentUser();
    }

    @PUT
    @Path("{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Updates a User",
                  notes = "Supplying the user object, this will update the user with the new information supplied " +
                          "in the body of the request.  Optionally, the user's password may be provided.")
    public User updateUser(final User user,
                           @PathParam("name") String name,
                           @QueryParam("password") String password) {

        validationService.validateModel(user);
        name = Strings.nullToEmpty(name).trim();
        password = Strings.nullToEmpty(password).trim();

        if (user.getName() == null) {
            user.setName(name);
        }

        if (Strings.isNullOrEmpty(name)) {
            throw new NotFoundException("User not found.");
        } else if (!(Objects.equals(user.getName(), name) || Objects.equals(user.getEmail(), name))) {
            throw new InvalidDataException("User name does not match the path.");
        }

        if (Strings.isNullOrEmpty(password)) {
            return userService.updateUser(user);
        } else {
            return userService.updateUser(user, password);
        }

    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Creates a User",
                  notes = "Supplying the user object, this will update the user with the new information supplied " +
                          "in the body of the request.  Optionally, the user's password may be provided.")
    public User createUser(final User user,
                           @QueryParam("password") String password) {

        validationService.validateModel(user);
        password = Strings.nullToEmpty(password).trim();

        if (password.trim().isEmpty()){
            return userService.createUser(user);
        } else {
            return userService.createUser(user, password);
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

}
