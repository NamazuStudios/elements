package dev.getelements.elements.rest.leaderboard;

import dev.getelements.elements.model.leaderboard.Score;
import dev.getelements.elements.rest.swagger.EnhancedApiListingResource;
import dev.getelements.elements.service.ScoreService;
import dev.getelements.elements.util.ValidationHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static dev.getelements.elements.rest.swagger.EnhancedApiListingResource.*;

@Api(value = "Scores",
     description = "Allows players to post new scores to a paricular leaderboard.",
     authorizations = {@Authorization(AUTH_BEARER), @Authorization(SESSION_SECRET), @Authorization(SOCIALENGINE_SESSION_SECRET)})
@Path("score")
public class ScoreResource {

    private ScoreService scoreService;

    private ValidationHelper validationHelper;

    @POST @Path("{leaderboardNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
        value = "Creates a New Score",
        notes = "Posts a single score for the currently logged-in profile. Conceptually, this is creationg a new " +
                "resource, however the server may opt to overwrite the existing identifier if it sees fit.")
    public Score createScore(
            @ApiParam("The name or id of the leaderboard.")
            @PathParam("leaderboardNameOrId")
            final String leaderboardNameOrId,
            final Score score) {
        return getScoreService().createOrUpdateScore(leaderboardNameOrId, score);
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public ScoreService getScoreService() {
        return scoreService;
    }

    @Inject
    public void setScoreService(ScoreService scoreService) {
        this.scoreService = scoreService;
    }

}
