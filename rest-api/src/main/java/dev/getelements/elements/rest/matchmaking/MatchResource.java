package dev.getelements.elements.rest.matchmaking;

import dev.getelements.elements.sdk.model.Constants;
import dev.getelements.elements.sdk.model.Headers;
import dev.getelements.elements.sdk.model.exception.InvalidParameterException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.match.Match;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.getelements.elements.sdk.service.match.MatchService;
import dev.getelements.elements.sdk.service.topic.Topic;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import static com.google.common.base.Strings.nullToEmpty;
import static dev.getelements.elements.sdk.model.Headers.REQUEST_LONG_POLL_TIMEOUT_DESCRIPTION;
import static java.lang.Math.min;
import static java.util.concurrent.TimeUnit.SECONDS;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;

/**
 * Manages match resources
 *
 * Created by patricktwohig on 7/18/17.
 */
@Path("match")
@Hidden
public class MatchResource {

    private int asyncTimeoutLimit;

    private MatchService matchService;

    private ValidationHelper validationHelper;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "List Matches",
            description = "Lists all matches available.  Under most circumstances, this will requires " +
                    "that a profile be made available to the request.  The server may choose to " +
                    "return an error if no suitable profile can be determined.")
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
    @Operation(
        summary = "Gets a Specific Match",
        description = "Gets a specific match given the match's unique ID.  Additionally, it is possible to instruct the " +
                "API to wait for a period of time before sending the response.  The request will intentionally hang " +
                "until the requested Match with ID has been updated in the database.")
    @ApiResponses({
            @ApiResponse(
                    content = @Content(
                            schema = @Schema(implementation = Match.class)
                    )
            )
    })
    public void getMatch(

            @PathParam("matchId")
            final String matchId,

            @Suspended
            final AsyncResponse asyncResponse,

            @HeaderParam(Headers.REQUEST_LONG_POLL_TIMEOUT)
            @Parameter(description = REQUEST_LONG_POLL_TIMEOUT_DESCRIPTION)
            final Long longPollTimeout) {

        final String _matchId = nullToEmpty(matchId).trim();

        if (_matchId.isEmpty()) {
            throw new NotFoundException();
        }

        final Match match = getMatchService().getMatch(_matchId);

        if (longPollTimeout == null) {
            asyncResponse.resume(match);
        } else {

            final Topic.Subscription subscription = getMatchService().attemptRematchAndPoll(
                _matchId,
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
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Creates a Match",
        description = "This method accepts an instance of Match, effectively requesting that the server find " +
                "a suitable opponent for a game.  As other suitable players create matches the created " +
                "match object may be updated as a suitable opponent is found.  The client must poll matches " +
                "for updates and react accordingly.")
    public Match createMatch(final Match match) {
        getValidationHelper().validateModel(match, ValidationGroups.Create.class);
        return getMatchService().createMatch(match);
    }

    @DELETE
    @Path("{matchId}")
    @Operation( summary = "Deletes a Match",
        description = "Deletes and permanently removes the Match fromt he server.  This effectively " +
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

}
