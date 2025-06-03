package dev.getelements.elements.rest.application;

import dev.getelements.elements.sdk.model.application.MatchmakingApplicationConfiguration;
import dev.getelements.elements.sdk.service.application.MatchmakingApplicationConfigurationService;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

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
    @Operation(
            summary = "Gets a iOS Application Configuration",
            description = "Gets a single iOS application based on unique name or ID.")
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
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Creates a new iOS ApplicationConfiguration",
            description = "Creates a new iOS ApplicationConfiguration with the specific ID or application.")
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
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Updates a iOS ApplicationConfiguration",
            description = "Updates an existing iOS Application profile if it is known to the server.")
    public MatchmakingApplicationConfiguration updateMatchmakingApplicationConfiguration(
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
    @Operation(
            summary = "Deletes a iOS ApplicationConfiguration",
            description = "Deletes an existing iOS Application profile if it is known to the server.")
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
