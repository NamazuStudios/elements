package dev.getelements.elements.rest.friends;

import dev.getelements.elements.sdk.model.exception.InvalidParameterException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.friend.Friend;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.getelements.elements.sdk.service.friend.FriendService;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import static com.google.common.base.Strings.nullToEmpty;

@Path("friend")
public class FriendResource {

    private ValidationHelper validationHelper;

    private FriendService friendService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation( summary = "Search Friends",
            description = "Searches all friends in the system and returning the metadata for all matches against the given " +
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
    @Operation(
            summary = "Gets a Specific Friend",
            description = "Gets a specific friend using the ID of the friend.")
    public Friend getFriend(@PathParam("friendId") final String friendId) {
        return getFriendService().getFriend(friendId);
    }

    @DELETE
    @Path("{friendId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Deletes a Friend",
            description = "Once a friend is deleted, re-creating a friend will set the friendship status to outgoing.")
    public void deleteFriendRegistration(
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
