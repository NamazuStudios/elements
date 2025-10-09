package dev.getelements.elements.rest.matchmaking;

import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.exception.InvalidParameterException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.match.MultiMatch;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.getelements.elements.sdk.service.match.MultiMatchService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import static com.google.common.base.Strings.nullToEmpty;

@Path("multi_match")
public class MultiMatchResource {

    private MultiMatchService matchService;

    private ValidationHelper validationHelper;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "List MultiMatches",
            description = "Lists all matches available.  Under most circumstances, this will requires " +
                    "that a profile be made available to the request.  The server may choose to " +
                    "return an error if no suitable profile can be determined.")
    public Pagination<MultiMatch> getMatches(
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
                getMultiMatchService().getMatches(offset, count) :
                getMultiMatchService().getMatches(offset, count, search);

    }

    @GET
    @Path("{matchId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Gets a Specific MultiMatch",
            description = "Gets a specific match given the match's unique ID.  Additionally, it is possible to instruct the " +
                    "API to wait for a period of time before sending the response.  The request will intentionally hang " +
                    "until the requested MultiMatch with ID has been updated in the database.")
    public MultiMatch getMatch(@PathParam("matchId") final String matchId) {

        final String _matchId = nullToEmpty(matchId).trim();

        if (_matchId.isEmpty()) {
            throw new NotFoundException();
        }

        return getMultiMatchService().getMatch(_matchId);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Creates a MultiMatch",
            description = "This method accepts an instance of MultiMatch and creates a new DB entry for it. Though " +
                    "it is generally recommended to create a new MultiMatch via matchmaking code in an Element, it " +
                    "can be created via REST for the purposes of testing or custom workflows.")
    public MultiMatch createMatch(final MultiMatch match) {
        getValidationHelper().validateModel(match, ValidationGroups.Create.class);
        return getMultiMatchService().createMatch(match);
    }

    @PUT
    @Path("{matchId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Updates a MultiMatch",
            description = "This method accepts an instance of MultiMatch and updates the DB entry for it that matches " +
                    "the matchId. Though it is generally recommended to update a MultiMatch via matchmaking code in " +
                    "an Element, it can be updated via REST for the purposes of testing or custom workflows.")
    public MultiMatch updateMatch(@PathParam("matchId") final String matchId, final MultiMatch match) {
        getValidationHelper().validateModel(match, ValidationGroups.Update.class);
        return getMultiMatchService().updateMatch(matchId, match);
    }

    @DELETE
    @Path("{matchId}")
    @Operation( summary = "Deletes a MultiMatch",
            description = "Deletes and permanently removes the MultiMatch from he server.  This effectively " +
                    "will cancel any pending request for a match.  If a game is currently being played " +
                    "against the match, the server may reject the request to delete the match until the game " +
                    "concludes.")
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteMatch(@PathParam("matchId") final String matchId) {
        getMultiMatchService().deleteMatch(matchId);
    }

    @DELETE
    @Operation( summary = "Deletes a MultiMatch",
            description = "Deletes and permanently removes all MultiMatches from he server.  This effectively " +
                    "will cancel any pending request for a match.  If a game is currently being played " +
                    "against the match, the server may reject the request to delete the match until the game " +
                    "concludes.")
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteAllMatches() {
        getMultiMatchService().deleteAllMatches();
    }

    public MultiMatchService getMultiMatchService() {
        return matchService;
    }

    @Inject
    public void setMultiMatchService(MultiMatchService matchService) {
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
