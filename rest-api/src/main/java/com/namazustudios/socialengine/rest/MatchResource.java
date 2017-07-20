package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.Constants;
import com.namazustudios.socialengine.ValidationHelper;
import com.namazustudios.socialengine.exception.InvalidParameterException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.exception.NotImplementedException;
import com.namazustudios.socialengine.model.Match;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.service.MatchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Strings.nullToEmpty;
import static java.lang.Long.max;

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

    private int asyncTimeoutLimit;

    private MatchService matchService;

    private ValidationHelper validationHelper;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "List Matches",
            notes = "Lists all matches available.  Under most circumstances, this will requires " +
                    "that a profile be made available to the request.  The server may choose to " +
                    "return an error if no suitable profile can be determined.")
    public void getMatches(

            @Suspended
            final AsyncResponse asyncResponse,

            @QueryParam("offset")
            @DefaultValue("0")
            @ApiParam("Ignored if long-polling.")
            final int offset,

            @QueryParam("count")
            @DefaultValue("20")
            @ApiParam("Ignored if long-polling.")
            final int count,

            @HeaderParam(XHttpHeaders.X_REQUEST_LONG_POLL_TIMEOUT)
            @ApiParam("Specifies the timeout of the request.")
            final Integer longPollTimeout,

            @QueryParam("lastUpdatedSince")
            @ApiParam("Filter by Matches updated since.  If unspecified, this will return matches " +
                      "since the provided date.")
            Date lastUpdatedSince) {

        if (offset < 0) {
            throw new InvalidParameterException("Offset must have positive value.");
        }

        if (count < 0) {
            throw new InvalidParameterException("Count must have positive value.");
        }

        if (lastUpdatedSince == null) {
            lastUpdatedSince = new Date(0);
        }

        if (longPollTimeout == null) {
            final Pagination<Match> matchPagination;
            matchPagination = getMatchService().getMatches(offset, count, lastUpdatedSince);
            asyncResponse.resume(matchPagination);
        } else {

            long timeout = max(longPollTimeout, getAsyncTimeoutLimit());
            asyncResponse.setTimeout(timeout, TimeUnit.SECONDS);

            getMatchService().waitForUpdates(
                lastUpdatedSince,
                matches -> asyncResponse.resume(Pagination.from(matches)),
                error -> asyncResponse.resume(error));

        }

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

    public int getAsyncTimeoutLimit() {
        return asyncTimeoutLimit;
    }

    @Inject
    public void setAsyncTimeoutLimit(@Named(Constants.ASYNC_TIMEOUT_LIMIT) int asyncTimeoutLimit) {
        this.asyncTimeoutLimit = asyncTimeoutLimit;
    }

    public MatchService getMatchService() {
        return matchService;
    }

    @Inject
    public void setMatchService(MatchService matchService) {
        this.matchService = matchService;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

}
