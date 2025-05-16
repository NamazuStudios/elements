package dev.getelements.elements.rest.followers;

import dev.getelements.elements.sdk.model.exception.InvalidParameterException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.follower.CreateFollowerRequest;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.getelements.elements.sdk.service.follower.FollowerService;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("follower")
public class FollowersResource {

    private ValidationHelper validationHelper;

    private FollowerService followerService;

    @GET
    @Path("{profileId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Search Followers",
            description = "Searches all followers in the system and returning the metadata for all matches against the given " +
                    "profile id.")
    public Pagination<Profile> getFollowers(
            @PathParam("profileId") final String profileId,
            @QueryParam("offset") @DefaultValue("0") final int offset,
            @QueryParam("count")  @DefaultValue("20") final int count) {

        if (offset < 0) {
            throw new InvalidParameterException("Offset must have positive value.");
        }

        if (count < 0) {
            throw new InvalidParameterException("Count must have positive value.");
        }

        return getFollowerService().getFollowers(profileId, offset, count);

    }
    @GET
    @Path("{profileId}/{followedId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Gets a specific profile using the ID of the profile and followed id.")
    public Profile getFollower(@PathParam("profileId") final String profileId,
                               @PathParam("followedId") final String followedId) {
        return getFollowerService().getFollower(profileId, followedId);
    }

    @POST
    @Path("{profileId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Creates a Follower relationship between two profiles.",
            description = "Supplying the follower object, this will store the information supplied " +
                    "in the body of the request.")
    public void createFollower(@PathParam("profileId") final String profileId, final CreateFollowerRequest createFollowerRequest) {
        getValidationHelper().validateModel(createFollowerRequest, ValidationGroups.Create.class);
        getFollowerService().createFollower(profileId, createFollowerRequest);
    }

    @DELETE
    @Path("{profileId}/{profileToUnfollowId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Deletes a Follower relationship")
    public void deleteFollower(
            @PathParam("profileId") final String profileId,
            @PathParam("profileToUnfollowId") final String profileToUnfollowId) {
        getFollowerService().deleteFollower(profileId, profileToUnfollowId);
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public FollowerService getFollowerService() {
        return followerService;
    }

    @Inject
    public void setFollowerService(FollowerService followerService) {
        this.followerService = followerService;
    }

}
