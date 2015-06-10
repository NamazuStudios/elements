package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.model.BasicEntrantProfile;
import com.namazustudios.socialengine.model.SocialCampaignEntry;
import com.namazustudios.socialengine.model.SteamEntrantProfile;
import com.namazustudios.socialengine.service.SocialCampaignService;
import com.namazustudios.socialengine.ValidationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by patricktwohig on 3/19/15.
 */
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
    public SocialCampaignEntry addEntrant(@PathParam("name")final String name, final BasicEntrantProfile basicEntrantProfile) {
        LOGGER.info("Adding entrant for basic campaign: " + name);
        validationService.validateModel(basicEntrantProfile);
        return socialCampaignService.submitEntrant(name, basicEntrantProfile);
    }

    @POST
    @Path("steam/entrant")
    @Produces(MediaType.APPLICATION_JSON)
    public SocialCampaignEntry addEntrant(@PathParam("name")final String name, final SteamEntrantProfile steamEntrant) {
        LOGGER.info("Adding entrant for Steam campaign: " + name);
        validationService.validateModel(steamEntrant);
        return socialCampaignService.submitEntrant(name, steamEntrant);
    }

}
