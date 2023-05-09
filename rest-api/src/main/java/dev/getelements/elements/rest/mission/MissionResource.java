package dev.getelements.elements.rest.mission;

import dev.getelements.elements.exception.InvalidParameterException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.mission.Mission;
import dev.getelements.elements.service.mission.MissionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import java.util.List;
import java.util.Set;

import static com.google.common.base.Strings.nullToEmpty;
import static dev.getelements.elements.rest.swagger.EnhancedApiListingResource.*;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;

@Path("mission")
@Api(value = "Missions",
        description = "Manages missions and steps",
        authorizations = {@Authorization(AUTH_BEARER), @Authorization(SESSION_SECRET), @Authorization(SOCIALENGINE_SESSION_SECRET)})
@Produces(MediaType.APPLICATION_JSON)
public class MissionResource {

    private MissionService missionService;

    @POST
    @ApiOperation(value = "Creates a new mission",
            notes = "Supplying a mission object, this will create a new mission with a newly assigned unique id.  " +
                    "The Mission representation returned in the response body is a representation of the Mission as persisted " +
                    "with a unique identifier assigned and with its fields properly normalized.  The supplied mission object " +
                    "submitted with the request must have a name property that is unique across all items.")
    public Mission createMission(Mission missionToBeCreated) {
        return missionService.createMission(missionToBeCreated);
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Search Missions",
            notes = "Searches all missions in the system and returning a number of matches against " +
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
    @ApiOperation(value = "Retrieves a single Mission by id or by name",
            notes = "Looks up a mission by the passed in identifier")
    public Mission getMissionByNameOrId(@PathParam("missionNameOrId") String missionNameOrId) {
        return missionService.getMissionByNameOrId(missionNameOrId);
    }

    @PUT
    @Path("{missionNameOrId}")
    @ApiOperation(value = "Updates an entire single Mission",
            notes = "Supplying a mission, this will update the Mission identified by the name or ID in the path with contents " +
                    "from the passed in request body. ")
    public Mission updateMission(final Mission updatedMission,
                           @PathParam("missionNameOrId") String missionNameOrId) {
        return missionService.updateMission(updatedMission);
    }

    @DELETE
    @Path("{missionNameOrId}")
    @ApiOperation(value = "Deletes the Mission identified by id or by name",
            notes = "Deletes a mission by the passed in identifier")
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
