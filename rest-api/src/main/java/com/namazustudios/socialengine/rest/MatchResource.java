package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.Constants;
import com.namazustudios.socialengine.Headers;
import com.namazustudios.socialengine.exception.InvalidParameterException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.match.Match;
import com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource;
import com.namazustudios.socialengine.service.MatchService;
import com.namazustudios.socialengine.service.Topic;
import com.namazustudios.socialengine.util.ValidationHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.Strings.nullToEmpty;
import static java.lang.Math.min;
import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

/**
 * Manages match resources
 *
 * Created by patricktwohig on 7/18/17.
 */
@Api(
    value = "Matches",
    description = "Drives the matchmaking API.  The matching system is used to match players of " +
                  "of roughly equal skill based on their past performance record.  LazyValue a Match is made, " +
                  "this allows two separate players (as represented by their profiles) to participate in a " +
                  "game.  Note, this API only provides matching.  The players must separately create a game " +
                  "from the match.  A match only provides a token which can be used to create a game which will " +
                  "be private among the two players involved.",
    authorizations = {@Authorization(EnhancedApiListingResource.SESSION_SECRET)})
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
                    "return an error if no suitable profile can be determined.",
            response = MatchesPagination.class)
    public Pagination<Match> getMatches(
            @QueryParam("offset")  @DefaultValue("0")  final int offset,
            @QueryParam("count")   @DefaultValue("20") final int count,
            @QueryParam("search") final String search) {

        if (offset < 0) {
            throw new InvalidParameterException("Offset must have positive value.");
        }

        if (count < 0) {
            throw new InvalidParameterException("Count must have positive value.");
        }

        final String query = nullToEmpty(search).trim();

        return query.isEmpty() ?
                getMatchService().getMatches(offset, count) :
                getMatchService().getMatches(offset, count, search);

    }

    @GET
    @Path("{matchId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
        value = "Gets a Specific Match",
        notes = "Gets a specific match given the match's unique ID.  Additionally, it is possible to instruct the " +
                "API to wait for a period of time before sending the response.  The request will intentionally hang " +
                "until the requested Match with ID has been updated in the database.",
        response = Match.class)
    public void getMatch(
            @PathParam("matchId")
            final String matchId,

            @Suspended
            final AsyncResponse asyncResponse,

            @DefaultValue("0")
            @QueryParam("lastUpdatedTimestamp")
            @ApiParam("Used in conjuction with the long poll paramter.  The match will return immediately only if " +
                      "supplied timestamp is before than the current time stamp.")
            final long lastUpdatedTimestamp,

            @HeaderParam(Headers.REQUEST_LONG_POLL_TIMEOUT)
            @ApiParam(Headers.REQUEST_LONG_POLL_TIMEOUT_DESCRIPTION)
            final Long longPollTimeout) {

        final String _matchId = nullToEmpty(matchId).trim();

        if (_matchId.isEmpty()) {
            throw new NotFoundException();
        }

        final Match match = getMatchService().getMatch(_matchId);

        if (longPollTimeout == null || lastUpdatedTimestamp < match.getLastUpdatedTimestamp()) {
            asyncResponse.resume(match);
        } else {

            final Topic.Subscription subscription = getMatchService().waitForUpdate(
                _matchId, lastUpdatedTimestamp,
                m -> asyncResponse.resume(m == null ? Response.status(NOT_FOUND).build() : m),
                ex -> asyncResponse.resume(ex));

            asyncResponse.setTimeoutHandler(r -> {
                subscription.close();
                r.resume(match);
            });

            asyncResponse.setTimeout(calculateLongPollTimeout(longPollTimeout), SECONDS);

        }

    }

    private long calculateLongPollTimeout(final long longPollTimeout) {
        return getAsyncTimeoutLimit() == 0 ? longPollTimeout : min(getAsyncTimeoutLimit(), longPollTimeout);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
        value = "Creates a Match",
        notes = "This method accepts an instance of Match, effectively requesting that the server find " +
                "a suitable opponent for a game.  As other suitable players create matches the created " +
                "match object may be updated as a suitable opponent is found.  The client must poll matches " +
                "for updates and react accordingly.")
    public Match createMatch(final Match match) {

        getValidationHelper().validateModel(match);

        final String matchId = nullToEmpty(match.getId()).trim();

        if (!matchId.isEmpty()) {
            throw new BadRequestException("match ID must be blank when creating a match");
        } else if (match.getOpponent() != null) {
            throw new BadRequestException("matches may not specify opponents on creation");
        }

        return getMatchService().createMatch(match);

    }

    @DELETE
    @Path("{matchId}")
    @ApiOperation(value = "Deletes a Match",
        notes = "Deletes and permanently removes the Match fromt he server.  This effectively " +
                "will cancel any pending request for a match.  If a game is currently being played " +
                "agaist the match, the server may reject the request to delete the match until the game " +
                "concludes.")
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteMatch(@PathParam("matchId") final String matchId) {
        getMatchService().deleteMatch(matchId);
    }

    public int getAsyncTimeoutLimit() {
        return asyncTimeoutLimit;
    }

    public void setAsyncTimeoutLimit(int asyncTimeoutLimit) {
        this.asyncTimeoutLimit = asyncTimeoutLimit;
    }

    @Inject
    public void setAsyncTimeoutLimitAsString(@Named(Constants.ASYNC_TIMEOUT_LIMIT) String asyncTimeoutLimit) {
        setAsyncTimeoutLimit(Integer.parseInt(asyncTimeoutLimit));
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

    private static final class MatchesPagination extends Pagination<Match> {}

}
