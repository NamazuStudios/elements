package dev.getelements.elements.rest.mission;

import dev.getelements.elements.exception.InvalidParameterException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.mission.CreateScheduleEventRequest;
import dev.getelements.elements.model.mission.ScheduleEvent;
import dev.getelements.elements.model.mission.UpdateScheduleEventRequest;
import dev.getelements.elements.rest.AuthSchemes;
import dev.getelements.elements.service.mission.ScheduleEventService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static com.google.common.base.Strings.nullToEmpty;

@Path("schedule/{scheduleNameOrId}/event")
@Api(value = "ScheduleEvent",
        description = "Manages ScheduleEvents",
        authorizations = {
                @Authorization(AuthSchemes.AUTH_BEARER),
                @Authorization(AuthSchemes.SESSION_SECRET),
                @Authorization(AuthSchemes.SOCIALENGINE_SESSION_SECRET)}
)
public class ScheduleEventResource {

    private ScheduleEventService scheduleService;

    @POST
    @ApiOperation(value = "Creates a new schedule",
            notes = "Supplying a schedule object, this will create a new schedule with a newly assigned unique id.  " +
                    "The ScheduleEvent representation returned in the response body is a representation of the ScheduleEvent as persisted " +
                    "with a unique identifier assigned and with its fields properly normalized.  The supplied schedule object " +
                    "submitted with the request must have a name property that is unique across all items.")
    public ScheduleEvent createScheduleEvent(
            @PathParam("scheduleNameOrId")
            final String scheduleNameOrId,
            final CreateScheduleEventRequest createScheduleEventRequest) {
        return scheduleService.createScheduleEvent(scheduleNameOrId, createScheduleEventRequest);
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Search ScheduleEvents",
            notes = "Searches all schedules in the system and returning a number of matches against " +
                    "the given search filter, delimited by the offset and count.")
    public Pagination<ScheduleEvent> getScheduleEvents(
            @PathParam("scheduleNameOrId")
            final String scheduleNameOrId,
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
                getscheduleService().getScheduleEvents(scheduleNameOrId, offset, count) :
                getscheduleService().getScheduleEvents(scheduleNameOrId, offset, count, search);

    }

    @GET
    @Path("{scheduleNameOrId}")
    @ApiOperation(value = "Retrieves a single ScheduleEvent by id or by name",
            notes = "Looks up a schedule by the passed in identifier")
    public ScheduleEvent getScheduleEventByNameOrId(
            @PathParam("scheduleNameOrId")
            final String scheduleNameOrId,
            @PathParam("scheduleNameOrId")
            final String scheduleEventNameOrId) {
        return scheduleService.getScheduleEventByNameOrId(scheduleNameOrId, scheduleEventNameOrId);
    }

    @PUT
    @Path("{scheduleNameOrId}")
    @ApiOperation(value = "Updates an entire single ScheduleEvent",
            notes = "Supplying a schedule, this will update the ScheduleEvent identified by the name or ID in the path with contents " +
                    "from the passed in request body. ")
    public ScheduleEvent updateScheduleEvent(
            @PathParam("scheduleNameOrId")
            final String scheduleNameOrId,
            @PathParam("scheduleNameOrId")
            final String scheduleEventNameOrId,
            final UpdateScheduleEventRequest updateScheduleEventRequest) {
        return scheduleService.updateScheduleEvent(scheduleNameOrId, scheduleEventNameOrId, updateScheduleEventRequest);
    }

    @DELETE
    @Path("{scheduleNameOrId}")
    @ApiOperation(value = "Deletes the ScheduleEvent identified by id or by name",
            notes = "Deletes a schedule by the passed in identifier")
    public void deleteScheduleEvent(@PathParam("scheduleNameOrId") String scheduleNameOrId) {
        scheduleService.deleteScheduleEvent(scheduleNameOrId);
    }

    public ScheduleEventService getscheduleService() {
        return scheduleService;
    }

    @Inject
    public void setScheduleEventService(final ScheduleEventService scheduleService) {
        this.scheduleService = scheduleService;
    }

}
