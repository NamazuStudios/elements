package dev.getelements.elements.rest.mission;

import dev.getelements.elements.sdk.model.exception.InvalidParameterException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.mission.Progress;

import dev.getelements.elements.sdk.service.mission.ScheduleProgressService;
import io.swagger.v3.oas.annotations.Operation;


import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("schedule/{scheduleNameOrId}/progress")
public class ScheduleProgressResource {

    private ScheduleProgressService scheduleProgressService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Gets all Progresses assigned via this schedule",
            description = "Fetches all current assignments to the currently logged-in profile.")
    public Pagination<Progress> getScheduleProgresses(
            @PathParam("scheduleNameOrId") final String scheduleNameOrId,
            @QueryParam("offset") @DefaultValue("0") final int offset,
            @QueryParam("count")  @DefaultValue("20") final int count,
            @QueryParam("tags") final List<String> tags) {

        if (offset < 0) {
            throw new InvalidParameterException("Offset must have positive value.");
        }

        if (count < 0) {
            throw new InvalidParameterException("Count must have positive value.");
        }

        return getScheduleProgressService().getScheduleProgressService(scheduleNameOrId, offset, count);

    }

    public ScheduleProgressService getScheduleProgressService() {
        return scheduleProgressService;
    }

    @Inject
    public void setScheduleProgressService(ScheduleProgressService scheduleProgressService) {
        this.scheduleProgressService = scheduleProgressService;
    }

}
