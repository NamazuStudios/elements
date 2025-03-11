package dev.getelements.elements.rest.mission;

import dev.getelements.elements.sdk.model.exception.InvalidParameterException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.mission.CreateScheduleEventRequest;
import dev.getelements.elements.sdk.model.mission.ScheduleEvent;
import dev.getelements.elements.sdk.model.mission.UpdateScheduleEventRequest;
import dev.getelements.elements.sdk.service.mission.ScheduleEventService;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

import static com.google.common.base.Strings.nullToEmpty;

@Path("schedule/{scheduleNameOrId}/event")
public class ScheduleEventResource {

    private ScheduleEventService scheduleService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Creates a new schedule",
            description =
                    "Supplying a schedule object, this will create a new schedule with a newly assigned unique id.  " +
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
    @Operation(
            summary = "Search ScheduleEvents",
            description =
                    "Searches all schedules in the system and returning a number of matches against " +
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
    @Path("{scheduleEventId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Retrieves a single ScheduleEvent by id or by name",
            description = "Looks up a schedule by the passed in identifier")
    public ScheduleEvent getScheduleEventByNameOrId(
            @PathParam("scheduleNameOrId")
            final String scheduleNameOrId,
            @PathParam("scheduleEventId")
            final String scheduleEventId) {
        return scheduleService.getScheduleEventByNameOrId(scheduleNameOrId, scheduleEventId);
    }

    @PUT
    @Path("{scheduleEventId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Updates an entire single ScheduleEvent",
            description = "Supplying a schedule, this will update the ScheduleEvent identified by the name or ID in the path with contents " +
                    "from the passed in request body. ")
    public ScheduleEvent updateScheduleEvent(
            @PathParam("scheduleNameOrId")
            final String scheduleNameOrId,
            @PathParam("scheduleEventId")
            final String scheduleEventId,
            final UpdateScheduleEventRequest updateScheduleEventRequest) {
        return scheduleService.updateScheduleEvent(scheduleNameOrId, scheduleEventId, updateScheduleEventRequest);
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{scheduleEventId}")
    @Operation(
            summary = "Deletes the ScheduleEvent identified by id or by name",
            description = "Deletes a schedule by the passed in identifier")
    public void deleteScheduleEvent(
            @PathParam("scheduleNameOrId")
            final String scheduleNameOrId,
            @PathParam("scheduleEventId")
            final String scheduleEventId) {
        scheduleService.deleteScheduleEvent(scheduleNameOrId, scheduleEventId);
    }

    public ScheduleEventService getscheduleService() {
        return scheduleService;
    }

    @Inject
    public void setScheduleEventService(final ScheduleEventService scheduleService) {
        this.scheduleService = scheduleService;
    }

}
