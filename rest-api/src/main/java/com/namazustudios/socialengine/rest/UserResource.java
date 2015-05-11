package com.namazustudios.socialengine.rest;

import com.google.common.base.Strings;
import com.namazustudios.socialengine.ValidationHelper;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.InvalidParameterException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.service.UserService;

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
public class UserResource {

    @Inject
    private UserService userService;

    @Inject
    private ValidationHelper validationService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Pagination<User> getUsers(
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("count") @DefaultValue("20") int count) {

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
    public User getUser(@PathParam("name") final String name) {
        return userService.getUser(name);
    }

    @GET
    @Path("me")
    @Produces(MediaType.APPLICATION_JSON)
    public User getUser() {
        return userService.getCurrentUser();
    }

    @PUT
    @Path("{name}")
    @Produces(MediaType.APPLICATION_JSON)
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
    public User createUser(final User user,
                           @PathParam("password") String password) {

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
    public void deactivateUser(@PathParam("name") final String name) {
        userService.deleteUser(name);
    }

}
