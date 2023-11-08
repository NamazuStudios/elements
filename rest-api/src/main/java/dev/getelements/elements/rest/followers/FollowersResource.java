package dev.getelements.elements.rest.followers;

import dev.getelements.elements.exception.InvalidParameterException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.ValidationGroups;
import dev.getelements.elements.model.follower.CreateFollowerRequest;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.rest.AuthSchemes;
import dev.getelements.elements.service.FollowerService;
import dev.getelements.elements.util.ValidationHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("follower")
@Api(value = "Followers",
        description = "Manages follower relationships among profiles.",
        authorizations = {
                @Authorization(AuthSchemes.AUTH_BEARER),
                @Authorization(AuthSchemes.SESSION_SECRET),
                @Authorization(AuthSchemes.SOCIALENGINE_SESSION_SECRET)
    }
)
public class FollowersResource {

    private ValidationHelper validationHelper;

    private FollowerService followerService;

    @GET
    @Path("{profileId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Search Followers",
            notes = "Searches all followers in the system and returning the metadata for all matches against the given " +
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
    @ApiOperation(
            value = "Gets a specific profile using the ID of the profile and followed id.")
    public Profile getFollower(@PathParam("profileId") final String profileId,
                               @PathParam("followedId") final String followedId) {
        return getFollowerService().getFollower(profileId, followedId);
    }

    @POST
    @Path("{profileId}")
    @ApiOperation(value = "Creates a Follower relationship between two profiles.",
            notes = "Supplying the follower object, this will store the information supplied " +
                    "in the body of the request.")
    public void createFollower(@PathParam("profileId") final String profileId, final CreateFollowerRequest createFollowerRequest) {
        getValidationHelper().validateModel(createFollowerRequest, ValidationGroups.Create.class);
        getFollowerService().createFollower(profileId, createFollowerRequest);
    }

    @DELETE
    @Path("{profileId}/{profileToUnfollowId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Deletes a Follower relationship")
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
