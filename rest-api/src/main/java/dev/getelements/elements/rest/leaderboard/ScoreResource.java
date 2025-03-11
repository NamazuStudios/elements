package dev.getelements.elements.rest.leaderboard;

import dev.getelements.elements.sdk.model.leaderboard.Score;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.getelements.elements.sdk.service.leaderboard.ScoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("score")
public class ScoreResource {

    private ScoreService scoreService;

    private ValidationHelper validationHelper;

    @POST @Path("{leaderboardNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Creates a New Score",
        description = "Posts a single score for the currently logged-in profile. Conceptually, this is creationg a new " +
                "resource, however the server may opt to overwrite the existing identifier if it sees fit.")
    public Score createScore(
            @Parameter(description = "The name or id of the leaderboard.")
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
