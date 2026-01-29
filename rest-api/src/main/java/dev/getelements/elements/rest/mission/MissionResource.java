package dev.getelements.elements.rest.mission;

import dev.getelements.elements.sdk.model.exception.InvalidParameterException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.mission.CreateMissionRequest;
import dev.getelements.elements.sdk.model.mission.Mission;
import dev.getelements.elements.sdk.model.mission.UpdateMissionRequest;
import dev.getelements.elements.sdk.service.mission.MissionService;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

import static com.google.common.base.Strings.nullToEmpty;
import static java.util.Collections.emptyList;

@Path("mission")
@Produces(MediaType.APPLICATION_JSON)
public class MissionResource {

    private MissionService missionService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Creates a new mission",
            description = "Supplying a mission object, this will create a new mission with a newly assigned unique id.  " +
                    "The Mission representation returned in the response body is a representation of the Mission as persisted " +
                    "with a unique identifier assigned and with its fields properly normalized.  The supplied mission object " +
                    "submitted with the request must have a name property that is unique across all items.")
    public Mission createMission(CreateMissionRequest missionToBeCreated) {
        return missionService.createMission(missionToBeCreated);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Search Missions",
            description = "Searches all missions in the system and returning a number of matches against " +
                    "the given search filter, delimited by the offset and count.")
    public Pagination<Mission> getMissions(
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
                getMissionService().getMissions(offset, count, tags == null ? emptyList() : tags) :
                getMissionService().getMissions(offset, count, search);

    }

    @GET
    @Path("{missionNameOrId}")
    @Operation(
            summary = "Retrieves a single Mission by id or by name",
            description = "Looks up a mission by the passed in identifier")
    public Mission getMissionByNameOrId(@PathParam("missionNameOrId") String missionNameOrId) {
        return missionService.getMissionByNameOrId(missionNameOrId);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{missionNameOrId}")
    @Operation(
            summary = "Updates an entire single Mission",
            description = "Supplying a mission, this will update the Mission identified by the name or ID in the path with contents " +
                    "from the passed in request body. ")
    public Mission updateMission(final UpdateMissionRequest updatedMission,
                           @PathParam("missionNameOrId") String missionNameOrId) {
        return missionService.updateMission(missionNameOrId, updatedMission);
    }

    @DELETE
    @Path("{missionNameOrId}")
    @Operation(
            summary = "Deletes the Mission identified by id or by name",
            description = "Deletes a mission by the passed in identifier")
    public void deleteMission(@PathParam("missionNameOrId") String missionNameOrId) {
        missionService.deleteMission(missionNameOrId);
    }


    public MissionService getMissionService() {
        return missionService;
    }

    @Inject
    public void setMissionService(MissionService missionService) {
        this.missionService = missionService;
    }

}
