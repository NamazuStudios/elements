package com.namazustudios.socialengine.rest.friends;

import com.namazustudios.socialengine.exception.InvalidParameterException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.friend.Friend;
import com.namazustudios.socialengine.service.FriendService;
import com.namazustudios.socialengine.util.ValidationHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static com.google.common.base.Strings.nullToEmpty;
import static com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource.*;

@Path("friend")
@Api(value = "Friends",
     description = "Manages friend ships among users.  Friends as associated among users, each with access to the " +
                   "individual profiles therein.",
     authorizations = {@Authorization(AUTH_BEARER), @Authorization(SESSION_SECRET), @Authorization(SOCIALENGINE_SESSION_SECRET)})
public class FriendResource {

    private ValidationHelper validationHelper;

    private FriendService friendService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Search Friends",
            notes = "Searches all friends in the system and returning the metadata for all matches against the given " +
                    "search filter.")
    public Pagination<Friend> getFriends(
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
                getFriendService().getFriends(offset, count) :
                getFriendService().getFriends(offset, count, search);

    }

    @GET
    @Path("{friendId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Gets a Specific Friend",
            notes = "Gets a specific friend using the ID of the friend.")
    public Friend getUser(@PathParam("friendId") final String friendId) {
        return getFriendService().getFriend(friendId);
    }

    @DELETE
    @Path("{friendId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Deletes a Friend",
            notes = "Once a friend is deleted, re-creating a friend will set the friendship status to outgoing.")
    public void deleteRegistration(
            @PathParam("friendId")
            final String friendId) {
        getFriendService().deleteFriend(friendId);
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public FriendService getFriendService() {
        return friendService;
    }

    @Inject
    public void setFriendService(FriendService friendService) {
        this.friendService = friendService;
    }

}
