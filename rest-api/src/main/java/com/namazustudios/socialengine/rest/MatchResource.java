package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.ValidationHelper;
import com.namazustudios.socialengine.exception.InvalidParameterException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.exception.NotImplementedException;
import com.namazustudios.socialengine.model.Match;
import com.namazustudios.socialengine.model.Pagination;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static com.google.common.base.Strings.nullToEmpty;

/**
 * Manages match resources
 *
 * Created by patricktwohig on 7/18/17.
 */
@Api(
    value = "Matches",
    description = "Drives the matchmaking API.  The matching system is used to match players of " +
                  "of roughly equal skill based on their past performance record.  Once a mactch is made, " +
                  "this allows two separate players (as represented by their profiles) to participate in a " +
                  "game.  Note, this API only provides matching.  The players must separately create a game " +
                  "from the match.")
@Path("match")
public class MatchResource {

    private ValidationHelper validationHelper;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
        value = "List Matches",
        notes = "Lists all matches availble.  Under most circumstances, this will requires " +
                "that a profile be made available to the request.  The server may choose to " +
                "return an error if no suitable profile can be determined.  Note that unlike other " +
                "similar calls, this does not allow searching.")
    public Pagination<Match> getMatches(
            @QueryParam("offset") @DefaultValue("0") final int offset,
            @QueryParam("count")  @DefaultValue("20") final int count) {

        if (offset < 0) {
            throw new InvalidParameterException("Offset must have positive value.");
        }

        if (count < 0) {
            throw new InvalidParameterException("Count must have positive value.");
        }

        throw new NotImplementedException();

    }

    @GET
    @Path("{matchId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
        value = "Gets a Specific Match",
        notes = "Gets a specific match given the match's unique ID.")
    public Match getMatch(@PathParam("matchId") String matchId) {

        matchId = nullToEmpty(matchId).trim();

        if (matchId.isEmpty()) {
            throw new NotFoundException();
        }

        throw new NotImplementedException();
//        return getProfileService().getProfile(profileId);

    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
        value = "Creates a Match",
        notes = "This method accepts an instance of Match, effectively requesting that the server find " +
                "a suitable opponent for a game.  As other suitable players create matches the created " +
                "match object may be updated as a suitable opponent is found.  The client must poll matches " +
                "for updates and react accordingly.")
    public Match createProfile(final Match match) {

        getValidationHelper().validateModel(match);

        final String profileId = nullToEmpty(match.getId()).trim();

        if (!profileId.isEmpty()) {
            throw new BadRequestException("Profile ID must be blank.");
        }

        throw new NotImplementedException();
//        return getProfileService().createProfile(profile);

    }

    @DELETE
    @Path("{profileId}")
    @ApiOperation(value = "Deletes a Match",
        notes = "Deletes and permanently removes the Match fromt he server.  This effectively " +
                "will cancel any pending request for a match.  If a game is currently being played " +
                "agaist the match, the server may reject the request to delete the match until the game " +
                "concludes.")
    public void deactivateProfile(@PathParam("profileId") final String matchId) {
        throw new NotImplementedException();
//        getProfileService().deleteProfile(profileId);
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

}
