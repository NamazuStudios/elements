package dev.getelements.elements.rest.mission;

import dev.getelements.elements.exception.InvalidParameterException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.mission.Progress;
import dev.getelements.elements.model.mission.ScheduleEvent;
import dev.getelements.elements.rest.AuthSchemes;
import dev.getelements.elements.service.mission.ScheduleProgressService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static com.google.common.base.Strings.nullToEmpty;

@Path("schedule/{scheduleNameOrId}/progress")
@Api(value = "ScheduleProgress",
        description = "Manages Schedule Progresses",
        authorizations = {
                @Authorization(AuthSchemes.AUTH_BEARER),
                @Authorization(AuthSchemes.SESSION_SECRET),
                @Authorization(AuthSchemes.SOCIALENGINE_SESSION_SECRET)}
)
public class ScheduleProgressResource {

    private ScheduleProgressService scheduleProgressService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Gets all Progresses assigned via this schedule",
            notes = "Fetches all current assignments to the currently logged-in profile.")
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
