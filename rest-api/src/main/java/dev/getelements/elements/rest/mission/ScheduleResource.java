package dev.getelements.elements.rest.mission;

import dev.getelements.elements.exception.InvalidParameterException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.mission.CreateScheduleRequest;
import dev.getelements.elements.model.mission.Schedule;
import dev.getelements.elements.model.mission.UpdateScheduleRequest;
import dev.getelements.elements.rest.AuthSchemes;
import dev.getelements.elements.service.mission.ScheduleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static com.google.common.base.Strings.nullToEmpty;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("schedule")
@Api(value = "Schedule",
        description = "Manages Schedules",
        authorizations = {
                @Authorization(AuthSchemes.AUTH_BEARER),
                @Authorization(AuthSchemes.SESSION_SECRET),
                @Authorization(AuthSchemes.SOCIALENGINE_SESSION_SECRET)}
)
public class ScheduleResource {

    private ScheduleService scheduleService;

    @POST
    @ApiOperation(value = "Creates a new schedule",
            notes = "Supplying a schedule object, this will create a new schedule with a newly assigned unique id.  " +
                    "The Schedule representation returned in the response body is a representation of the Schedule as persisted " +
                    "with a unique identifier assigned and with its fields properly normalized.  The supplied schedule object " +
                    "submitted with the request must have a name property that is unique across all items.")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Schedule createSchedule(CreateScheduleRequest createScheduleRequest) {
        return scheduleService.createSchedule(createScheduleRequest);
    }


    @GET
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Search Schedules",
            notes = "Searches all schedules in the system and returning a number of matches against " +
                    "the given search filter, delimited by the offset and count.")
    public Pagination<Schedule> getSchedules(
            @QueryParam("offset") @DefaultValue("0") final int offset,
            @QueryParam("count")  @DefaultValue("20") final int count,
            @QueryParam("tags") final List<String> tags,
            @QueryParam("search") final String search) {

        if (offset < 0) {
            throw new InvalidParameterException("Offset must have positive value.");
        }

        if (count < 0) {
            throw new InvalidParameterException("Count must have positive value.");
        }

        final String query = nullToEmpty(search).trim();

        return query.isEmpty() ?
                getscheduleService().getSchedules(offset, count) :
                getscheduleService().getSchedules(offset, count, search);

    }

    @GET
    @Path("{scheduleNameOrId}")
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Retrieves a single Schedule by id or by name",
            notes = "Looks up a schedule by the passed in identifier")
    public Schedule getScheduleByNameOrId(@PathParam("scheduleNameOrId") String scheduleNameOrId) {
        return scheduleService.getScheduleByNameOrId(scheduleNameOrId);
    }

    @PUT
    @Path("{scheduleNameOrId}")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Updates an entire single Schedule",
            notes = "Supplying a schedule, this will update the Schedule identified by the name or ID in the path with contents " +
                    "from the passed in request body. ")
    public Schedule updateSchedule(final UpdateScheduleRequest updateScheduleRequest,
                                 @PathParam("scheduleNameOrId") String scheduleNameOrId) {
        return scheduleService.updateSchedule(scheduleNameOrId, updateScheduleRequest);
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{scheduleNameOrId}")
    @ApiOperation(value = "Deletes the Schedule identified by id or by name",
            notes = "Deletes a schedule by the passed in identifier")
    public void deleteSchedule(@PathParam("scheduleNameOrId") String scheduleNameOrId) {
        scheduleService.deleteSchedule(scheduleNameOrId);
    }

    public ScheduleService getscheduleService() {
        return scheduleService;
    }

    @Inject
    public void setScheduleService(final ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

}
