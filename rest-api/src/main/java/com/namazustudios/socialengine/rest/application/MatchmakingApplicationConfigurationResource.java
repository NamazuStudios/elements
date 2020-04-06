package com.namazustudios.socialengine.rest.application;

import com.namazustudios.socialengine.model.application.MatchmakingApplicationConfiguration;
import com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource;
import com.namazustudios.socialengine.service.MatchmakingApplicationConfigurationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource.SESSION_SECRET;
import static com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource.SOCIALENGINE_SESSION_SECRET;

@Api(value = "Matchmaking Application Configuration",
        description = "These operations manage any variety of client side applications which " +
                "may be communicating with the server.  This stores minimal information " +
                "for each and is used primairly as an aggregation point for other application " +
                "profiles.  Application metadata is typically used for client side apps to determine " +
                "the latest version or to resolve any compatiblity issues.  This can also be used to " +
                "perform force upgrades.",
        authorizations = {@Authorization(SESSION_SECRET), @Authorization(SOCIALENGINE_SESSION_SECRET)})
@Path("application/{applicationNameOrId}/configuration/matchmaking")
public class MatchmakingApplicationConfigurationResource {

    private MatchmakingApplicationConfigurationService matchmakingApplicationConfigurationService;

    /**
     * Gets the specific {@link MatchmakingApplicationConfiguration} instances assocated with the
     * application.
     *
     * @param applicationNameOrId the application name or ID
     * @param applicationConfigurationNameOrId the application profile name or ID
     *
     * @return the {@link MatchmakingApplicationConfiguration} instance
     */
    @GET
    @Path("{applicationConfigurationNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Gets a iOS Application Configuration",
            notes = "Gets a single iOS application based on unique name or ID.")
    public MatchmakingApplicationConfiguration getMatchmakingApplicationConfiguration(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("applicationConfigurationNameOrId") final String applicationConfigurationNameOrId) {
        return getMatchmakingApplicationConfigurationService().getApplicationConfiguration(applicationNameOrId, applicationConfigurationNameOrId);
    }

    /**
     * Creates a new {@link MatchmakingApplicationConfiguration} isntance.
     *
     * @param applicationNameOrId the applciation name or ID
     * @param matchmakingApplicationConfigurations the iOS appliation profile object to creates
     *
     * @return the {@link MatchmakingApplicationConfiguration} the iOS Application Configuration
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Creates a new iOS ApplicationConfiguration",
            notes = "Creates a new iOS ApplicationConfiguration with the specific ID or application.")
    public MatchmakingApplicationConfiguration createMatchmakingApplicationConfiguration(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            final MatchmakingApplicationConfiguration matchmakingApplicationConfigurations) {
        return getMatchmakingApplicationConfigurationService().createApplicationConfiguration(applicationNameOrId, matchmakingApplicationConfigurations);
    }

    /**
     * Updates an existing {@link MatchmakingApplicationConfiguration} isntance.
     *
     * @param applicationNameOrId the applciation name or ID
     * @param applicationConfigurationNameOrId the name or identifier of the {@link MatchmakingApplicationConfiguration}
     * @param matchmakingApplicationConfiguration the iOS application profile object to update
     *
     * @return the {@link MatchmakingApplicationConfiguration} the iOS Application Configuration
     */
    @PUT
    @Path("{applicationConfigurationNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Updates a iOS ApplicationConfiguration",
            notes = "Updates an existing iOS Application profile if it is known to the server.")
    public MatchmakingApplicationConfiguration updateApplicationConfiguration(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("applicationConfigurationNameOrId") final String applicationConfigurationNameOrId,
            final MatchmakingApplicationConfiguration matchmakingApplicationConfiguration) {
        return getMatchmakingApplicationConfigurationService().updateApplicationConfiguration(
                applicationNameOrId,
                applicationConfigurationNameOrId,
                matchmakingApplicationConfiguration);
    }

    /**
     * Deletes an instance of {@link MatchmakingApplicationConfiguration}.
     *
     * @param applicationNameOrId the application ID, or name
     * @param applicationConfigurationNameOrId the application profile ID, or name
     */
    @DELETE
    @Path("{applicationConfigurationNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Deletes a iOS ApplicationConfiguration",
            notes = "Deletes an existing iOS Application profile if it is known to the server.")
    public void deleteMatchmakingApplicationConfiguration(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("applicationConfigurationNameOrId") final String applicationConfigurationNameOrId) {
        getMatchmakingApplicationConfigurationService().deleteApplicationConfiguration(applicationNameOrId, applicationConfigurationNameOrId);
    }

    public MatchmakingApplicationConfigurationService getMatchmakingApplicationConfigurationService() {
        return matchmakingApplicationConfigurationService;
    }

    @Inject
    public void setMatchmakingApplicationConfigurationService(MatchmakingApplicationConfigurationService matchmakingApplicationConfigurationService) {
        this.matchmakingApplicationConfigurationService = matchmakingApplicationConfigurationService;
    }

}
