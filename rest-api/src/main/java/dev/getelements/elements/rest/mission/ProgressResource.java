package dev.getelements.elements.rest.mission;

import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.Tabulation;
import dev.getelements.elements.model.leaderboard.RankRow;
import dev.getelements.elements.model.mission.Progress;
import dev.getelements.elements.model.mission.ProgressRow;
import dev.getelements.elements.rest.AuthSchemes;
import dev.getelements.elements.service.progress.ProgressService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("progress")
@Api(value = "Progress",
        description = "Manages progress",
        authorizations = {
                @Authorization(AuthSchemes.AUTH_BEARER),
                @Authorization(AuthSchemes.SESSION_SECRET),
                @Authorization(AuthSchemes.SOCIALENGINE_SESSION_SECRET)
})
@Produces(MediaType.APPLICATION_JSON)
public class ProgressResource {

    private ProgressService progressService;

    @POST
    @ApiOperation(value = "Creates a new progress",
            notes = "Supplying a progress object, this will create a new progress with a newly assigned unique id.  " +
                    "The Progress representation returned in the response body is a representation of the Progress as persisted " +
                    "with a unique identifier assigned and with its fields properly normalized.")
    public Progress createProgress(Progress progressToBeCreated) {
        return getProgressService().createProgress(progressToBeCreated);
    }


    @GET
    @ApiOperation(value = "Retrieves all Progresses",
            notes = "Searches all progress and returns all matching items, filtered by the passed in search parameters.")
    public Pagination<Progress> getProgress(
            @QueryParam("offset") @DefaultValue("0")  final int offset,
            @QueryParam("count")  @DefaultValue("20") final int count,
            @QueryParam("tags")   final List<String> tags,
            @QueryParam("search") final String search) {
        return getProgressService().getProgresses(offset, count, tags, search);
    }

    @GET
    @Produces("text/csv")
    @ApiOperation(value = "Gets Rank Among all Players",
            notes = "Gets the current Profile's rank among all players for the particular leaderboard.")
    public Tabulation<ProgressRow> getProgressTabular() {
        return getProgressService().getProgressesTabular();
    }

    @GET
    @Path("{progressId}")
    @ApiOperation(value = "Retrieves a single Progress by id",
            notes = "Looks up a progress by the passed in identifier")
    public Progress getProgressByNameOrId(@PathParam("progressId") String progressId) {
        return getProgressService().getProgress(progressId);
    }

    @PUT
    @Path("{progressId}")
    @ApiOperation(value = "Updates a single Progress",
            notes = "Supplying a progress, this will update the Progress identified by the ID in the path with contents " +
                    "from the passed in request body. ")
    public Progress updateProgress(final Progress updatedProgress,
                              @PathParam("progressId") String progressId) {
        return getProgressService().updateProgress(updatedProgress);
    }

    @DELETE
    @Path("{progressId}")
    @ApiOperation(value = "Deletes the Progress identified by id",
            notes = "Deletes a progress by the passed in identifier")
    public void deleteProgress(@PathParam("progressId") String progressId) {
        getProgressService().deleteProgress(progressId);
    }

    public ProgressService getProgressService() {
        return progressService;
    }

    @Inject
    public void setProgressService(ProgressService progressService) {
        this.progressService = progressService;
    }

}
