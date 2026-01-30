package dev.getelements.elements.rest.mission;

import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.Tabulation;
import dev.getelements.elements.sdk.model.mission.CreateProgressRequest;
import dev.getelements.elements.sdk.model.mission.Progress;
import dev.getelements.elements.sdk.model.mission.ProgressRow;

import dev.getelements.elements.sdk.model.mission.UpdateProgressRequest;
import dev.getelements.elements.sdk.service.progress.ProgressService;
import io.swagger.v3.oas.annotations.Operation;


import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("progress")
@Produces(MediaType.APPLICATION_JSON)
public class ProgressResource {

    private ProgressService progressService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation( summary = "Creates a new progress",
            description = "This will create a new progress with a compound id of the profile and mission.  " +
                    "The Progress representation returned in the response body is a representation of the Progress as persisted " +
                    "with a unique identifier assigned and with its fields properly normalized.")
    public Progress createProgress(CreateProgressRequest progressToBeCreated) {

        return getProgressService().createProgress(progressToBeCreated);
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation( summary = "Retrieves all Progresses",
            description = "Searches all progress and returns all matching items, filtered by the passed in search parameters.")
    public Pagination<Progress> getProgress(@QueryParam("offset") @DefaultValue("0")  final int offset,
                                            @QueryParam("count")  @DefaultValue("20") final int count,
                                            @QueryParam("tags")   final List<String> tags,
                                            @QueryParam("search") final String search) {

        return getProgressService().getProgresses(offset, count, tags, search);
    }

    @GET
    @Produces("text/csv")
    @Operation( summary = "Gets Rank Among all Players",
            description = "Gets the current Profile's rank among all players for the particular leaderboard.")
    public Tabulation<ProgressRow> getProgressTabular() {

        return getProgressService().getProgressesTabular();
    }

    @GET
    @Path("{progressId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation( summary = "Retrieves a single Progress by id",
            description = "Looks up a progress by the passed in identifier")
    public Progress getProgressByNameOrId(@PathParam("progressId") String progressId) {

        return getProgressService().getProgress(progressId);
    }

    @PUT
    @Path("{progressId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation( summary = "Updates a single Progress",
            description = "This will update the Progress identified by the ID in the path with contents " +
                    "from the passed in request body. ")
    public Progress updateProgress(@PathParam("progressId") String progressId,
                                   final UpdateProgressRequest updatedProgress) {

        return getProgressService().updateProgress(progressId, updatedProgress);
    }

    @DELETE
    @Path("progress/{progressId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation( summary = "Deletes the Progress identified by id",
            description = "Deletes a progress by the passed in identifier")
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
