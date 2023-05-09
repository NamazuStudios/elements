package dev.getelements.elements.rest;

import dev.getelements.elements.model.BasicEntrantProfile;
import dev.getelements.elements.model.SocialCampaignEntry;
import dev.getelements.elements.model.SteamEntrantProfile;
import dev.getelements.elements.rest.swagger.EnhancedApiListingResource;
import dev.getelements.elements.service.SocialCampaignService;
import dev.getelements.elements.util.ValidationHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import static dev.getelements.elements.rest.swagger.EnhancedApiListingResource.*;

/**
 * Created by patricktwohig on 3/19/15.
 */
@Api(value = "Entrants",
     description = "Allows users to register for entry into social camapaigs.  Social " +
                   "campaigns are essentially simple promotions which allow a user to " +
                   "receive some sort of reward or incentive for sharing a specific link " +
                   "which is tracked through a short linker.",
     authorizations = {@Authorization(AUTH_BEARER), @Authorization(SESSION_SECRET), @Authorization(SOCIALENGINE_SESSION_SECRET)})
@Path("campaign/{name}")
public class EntrantResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntrantResource.class);

    @Inject
    private SocialCampaignService socialCampaignService;

    @Inject
    private ValidationHelper validationService;

    @POST
    @Path("basic/entrant")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Creates a Basic Entrant",
                  notes = "A basic entrant is keyed uniquely from email and provides very simple " +
                          "contact information.  The entrant can be associated with a User later if " +
                          "necessary.")
    public SocialCampaignEntry addBasicEntrant(
            @PathParam("name")final String name, final BasicEntrantProfile basicEntrantProfile) {
        LOGGER.info("Adding entrant for basic campaign: " + name);
        validationService.validateModel(basicEntrantProfile);
        return socialCampaignService.submitEntrant(name, basicEntrantProfile);
    }

    @POST
    @Path("steam/entrant")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Creates a Steam Entrant",
                  notes = "A Steam entrant is simalar to a basic entrant, but captures the user's Steam " +
                          "ID in addition to the remaining basic info.")
    public SocialCampaignEntry addSteamEntrant(
            @PathParam("name")final String name, final SteamEntrantProfile steamEntrant) {
        LOGGER.info("Adding entrant for Steam campaign: " + name);
        validationService.validateModel(steamEntrant);
        return socialCampaignService.submitEntrant(name, steamEntrant);
    }

}
